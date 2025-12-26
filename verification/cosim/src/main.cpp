#include <iostream>

#include "../src/top.cpp"
#include "VTrinity.h"
#include "fb.hpp"
#include "types/ap_fixed.h"
#include "types/ap_int.h"
#include "verilated.h"
#include "verilated_vcd_c.h"
// For verify geometry stage
#include "VTrinity_GenericRam.h"
#include "VTrinity_GenericRam_1.h"
#include "VTrinity_GenericRam_2.h"
#include "VTrinity_GenericRam_3.h"
#include "VTrinity_Renderer.h"
#include "VTrinity_Trinity.h"
#include "VTrinity_mem_combMem.h"
#include "VTrinity_mem_combMem_0.h"
#include "VTrinity_mem_combMem_1.h"
#include "VTrinity_mem_combMem_2.h"

#define SIMUALTION_FRAME_NUM 1
#define SIMUALTION_START_ENGLE 0
#define WIDTH 1024
#define HEIGHT 768
#define VRAM_SIZE 256 * 1024 * 1024 / 16

static ap_uint<128> vram[VRAM_SIZE];

class SimDriver {
    public:
        VerilatedContext* contextp;
        VTrinity* top;
        VerilatedVcdC* tfp;
        uint64_t total_cycles;

        SimDriver(int argc, char** argv) {
            contextp = new VerilatedContext;
            contextp->commandArgs(argc, argv);

            top = new VTrinity{contextp};

            // Init Trace
            Verilated::traceEverOn(true);
            tfp = new VerilatedVcdC;
            top->trace(tfp, 99);
            tfp->open("trace.vcd");

            total_cycles = 0;
            top->clock = 0;
            top->reset = 1;
        }

        ~SimDriver() {
            top->final();
            tfp->close();
            delete top;
            delete tfp;
            delete contextp;
        }

        void step(int n = 1) {
            for (int i = 0; i < n; i++) {
                // Rising Edge (0 -> 1)
                top->clock = 1;
                top->eval();
                tfp->dump(contextp->time());
                contextp->timeInc(1);

                // Falling Edge (1 -> 0)
                top->clock = 0;
                top->eval();
                tfp->dump(contextp->time());
                contextp->timeInc(1);

                total_cycles++;
            }
        }

        void eval_and_dump() {
            top->eval();
            tfp->dump(contextp->time());
            contextp->timeInc(1);
        }

        vluint64_t time() { return contextp->time(); }
};

/**
 * Verify by compare the data in framebuffer c++ simulation (golden refernce)
 * with chisel framebuffer.
 * - c++ simulation framebuffer can accessed by global varible "vram"
 * - Chisel framebuffer can be accessed by debug interface at
 *   sim.top->io_debug_*
 *
 * Now this function doesn't use golden refernce to test, it verify by testing
 * all the pixels are expected value.
 */
void verify_framebuffer(SimDriver& sim, int frame, uint32_t base_addr) {
    std::cout << "[INFO] Verifying frambuffer ...\n";
    uint32_t expected = 0xff00ff;
    uint64_t err_cnt = 0;

    for (int y = 0; y < HEIGHT; y++) {
        for (int x = 0; x < WIDTH; x += 4) {
            uint32_t word_idx = (y * WIDTH + x + base_addr) / 4;
            sim.top->io_debug_idx = word_idx;

            sim.eval_and_dump();

            for (int i = 0; i < 4; i++) {
                // Accessing wide signal array
                uint32_t pixel_val = sim.top->io_debug_data[i];
                if (pixel_val != expected) {
                    if (err_cnt < 5) {
                        std::cerr << " Mismatch at (" << x + i << ", "
                                  << y << ") Got: 0x" << std::hex << pixel_val
                                  << " Exp: 0x" << expected << std::dec << "\n";
                        if (err_cnt == 4) {
                            std::cerr << " ...\n";
                        }
                    }
                    err_cnt++;
                }
            }
        }
    }

    if (err_cnt > 0)
        std::cout << "[ERROR] Frame " << frame << " error count " << err_cnt
                  << "\n";
    else
        std::cout << "[INFO] Frame " << frame << " error count 0" << "\n";

    std::cout << "[INFO] Frame " << frame << " verification done.\n";
}

/**
 * Verify geometry stage in chisel by comparing the data in intermediate
 * buffers, such as memVertex, memPos, memNorm, memBB with the global variable
 * using in top.cpp.
 */
void verify_geometry(SimDriver& sim, int frame, uint32_t base_addr) {
    std::cout << "[INFO] Verifying geometry stage ...\n";
    auto renderer = sim.top->__PVT__Trinity->__PVT__renderer;

    /* memVertex */
    if (auto wrapper = renderer->__PVT__memVertex) {
        auto real_ram = wrapper->__PVT__mem_ext;
        std::cout << "- memVertex compare to screen_vertices ...\n";
        for (int i = 0; i < NR_MESH_VERTICES; i++) {
            /* Tesing values */
            const uint32_t* raw = real_ram->Memory[i].data();
            uint32_t x = (uint32_t)raw[0];
            uint32_t y = (uint32_t)raw[1];
            uint32_t z = raw[2] & 0xFFFFFF;

            /* Expected values */
            uint32_t expected_x = (uint32_t)(screen_vertices[i].pos.x);
            uint32_t expected_y = (uint32_t)(screen_vertices[i].pos.y);
            uint32_t expected_z = (uint32_t)(screen_vertices[i].z.range(23, 0));

            if (expected_x == x && expected_y == y && expected_z == z) continue;

            std::cout << " wrong memVertex[" << i << "] \n";
            std::cout << std::hex << "  Get pos.x=0x" << x << " pos.y=0x" << y
                      << " z=0x" << z << "\n";
            std::cout << "  Exp pos.x=0x" << expected_x << " pos.y=0x"
                      << expected_y << " z=0x" << expected_z << std::dec
                      << "\n";
        }
    }

    /* memPos */
    if (auto wrapper = renderer->__PVT__memPos) {
        auto real_ram = wrapper->__PVT__mem_ext;
        std::cout << "- memPos compare to transformed_positions ...\n";
        for (int i = 0; i < NR_MESH_VERTICES; i++) {
            /* Tesing values */
            const uint32_t* raw = real_ram->Memory[i].data();
            uint32_t raw_x = raw[0] & 0xFFFFFF;
            uint32_t raw_y = (raw[0] >> 24) | ((raw[1] & 0xFFFF) << 8);
            raw_y &= 0xFFFFFF;
            uint32_t raw_z = (raw[1] >> 16) | ((raw[2] & 0xFF) << 16);
            raw_z &= 0xFFFFFF;

            /* Expected values */
            uint32_t expected_x =
                (uint32_t)(transformed_positions[i].x.range(23, 0));
            uint32_t expected_y =
                (uint32_t)(transformed_positions[i].y.range(23, 0));
            uint32_t expected_z =
                (uint32_t)(transformed_positions[i].z.range(23, 0));

            if (expected_x == raw_x && expected_y == raw_y &&
                expected_z == raw_z)
                continue;

            std::cout << " Wrong memPos[" << i << "] \n";
            std::cout << std::hex << "  Get X=0x" << raw_x << " "
                      << "Y=0x" << raw_y << " "
                      << "Z=0x" << raw_z << "\n";
            std::cout << "  Exp x=0x" << expected_x << " y=0x" << expected_y
                      << " z=0x" << expected_z << std::dec << '\n';
        }
    }

    /* memNorm */
    if (auto wrapper = renderer->__PVT__memNorm) {
        auto real_ram = wrapper->__PVT__mem_ext;
        std::cout << "- memNorm compare to transformed_normals ...\n";
        for (int i = 0; i < NR_MESH_NORMALS; i++) {
            /* Tesing values */
            const uint32_t* raw = real_ram->Memory[i].data();

            uint32_t raw_x = raw[0] & 0xFFFFFF;
            uint32_t raw_y = (raw[0] >> 24) | ((raw[1] & 0xFFFF) << 8);
            raw_y &= 0xFFFFFF;
            uint32_t raw_z = (raw[1] >> 16) | ((raw[2] & 0xFF) << 16);
            raw_z &= 0xFFFFFF;

            /* Expected values */
            uint32_t expected_x =
                (uint32_t)(transformed_normals[i].x.range(23, 0));
            uint32_t expected_y =
                (uint32_t)(transformed_normals[i].y.range(23, 0));
            uint32_t expected_z =
                (uint32_t)(transformed_normals[i].z.range(23, 0));

            if (expected_x == raw_x && expected_y == raw_y &&
                expected_z == raw_z)
                continue;

            std::cout << " Wrong memNorm[" << i << "] \n";
            std::cout << std::hex << "  Get x=0x" << raw_x << " "
                      << "y=0x" << raw_y << " "
                      << "z=0x" << raw_z << "\n";
            std::cout << "  Exp x=0x" << expected_x << " y=0x" << expected_y
                      << " z=0x" << expected_z << std::dec << '\n';
        }
    }

    /* memBB */
    if (auto wrapper = renderer->__PVT__memBB) {
        auto real_ram = wrapper->__PVT__mem_ext;
        std::cout << "- memBB compare to bounding_boxes ...\n";
        for (int i = 0; i < NR_MESH_TRIANGLES; i++) {
            /* Tesing values */
            const uint32_t* raw = real_ram->Memory[i].data();
            uint32_t v0 = (uint32_t)(raw[0]);
            uint32_t v1 = (uint32_t)(raw[1]);
            uint32_t v2 = (uint32_t)(raw[2]);
            uint32_t v3 = (uint32_t)(raw[3]);

            /* Expected values */
            uint32_t expected_0 = (uint32_t)(bounding_boxes[i].min.x);
            uint32_t expected_1 = (uint32_t)(bounding_boxes[i].min.y);
            uint32_t expected_2 = (uint32_t)(bounding_boxes[i].max.x);
            uint32_t expected_3 = (uint32_t)(bounding_boxes[i].max.y);

            if (expected_0 == v0 && expected_1 == v1 && expected_2 == v2 &&
                expected_3 == v3)
                continue;

            std::cout << " Wrong memBB[" << i << "] \n";
            std::cout << std::hex << "  Get 0x" << v0 << " 0x" << v1 << " 0x"
                      << v2 << " 0x" << v3 << "\n";
            std::cout << "  Exp 0x" << expected_0 << " 0x" << expected_1
                      << " 0x" << expected_2 << " 0x" << expected_3 << std::dec
                      << "\n";
        }
    }
}

int main(int argc, char** argv) {
    Verilated::commandArgs(argc, argv);

    /* Init C++ simualtion parameter */
    int fb_id = 1;

    /* Init Varilator simulation */
    SimDriver sim(argc, argv);

    // Reset
    sim.top->io_debug_displayVsync = 0;
    sim.top->io_debug_idx = 0;
    sim.step(50);
    sim.top->reset = 0;

    for (int frame = 0; frame < SIMUALTION_FRAME_NUM; frame++) {
        std::cout << "[INFO] Rendering Frame " << frame << "...\n";

        /* C++ Simulation as golden renference*/
        std::cout << "[INFO] Start C++ simualtion ...\n";
        trinity_renderer(fb_id, vram, SIMUALTION_START_ENGLE);
        // Change fb_id
        fb_id = (fb_id + 1) % 2;

        /* Varilator simulation*/
        std::cout << "[INFO] Start Varilator simulation ...\n";

        int timeout = 200000000;
        int cycles = 0;

        /* Wait until renderer draw a frame into frame buffer */
        while (!sim.top->io_debug_graphicsDone && cycles < timeout) {
            sim.step();
            cycles++;
        }
        if (cycles >= timeout) {
            std::cerr << "[ERROR] Timeout! Renderer did not finish."
                      << std::endl;
            break;
        }

        /* Calculate the base address of fb_id */
        uint32_t base_addr = 0;
        if (sim.top->io_debug_graphicsFbId == 1) {
            base_addr = (1 << 20);
        }

        std::cout << "[INFO] Frame " << frame << " finished at cycle "
                  << sim.total_cycles << '\n';

        /* Verification */
        std::cout << "[INFO] Start verification ...\n";

        verify_geometry(sim, frame, base_addr);
        verify_framebuffer(sim, frame, base_addr);

        /*
         * Pretending to be the valid signal of display component. Because
         * we disable display comonent temporarily, it need to play a role
         * of trigger enable renderer to start process next frame.
         */
        // Renderer Done && Display Vsync -> Swap Buffer -> Start New Frame
        sim.top->io_debug_displayVsync = 1;
        sim.step(10);
        sim.top->io_debug_displayVsync = 0;
        sim.step(10);
    }

    return 0;
}
