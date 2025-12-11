#include <iostream>
#include <future>
#include "SoftwareRenderer.hpp"
#include "HardwareRenderer.hpp"

#ifndef VRAM_SIZE
#define VRAM_SIZE (256 * 1024 * 1024 / 16)
#endif

void compare_results(ap_uint<128>* hw_mem, ap_uint<128>* sw_mem) {
    int error_count = 0;
    int max_errors = 5; // Report only first 5 errors to avoid flooding console

    for (int i = 0; i < VRAM_SIZE; i++) {
        if (hw_mem[i] != sw_mem[i]) {
            if (error_count < max_errors) {
                std::cout << "[Mismatch] offset: 0x" << std::hex << i * 16 
                            << " | HW: " << hw_mem[i].to_string(16)
                            << " | SW: " << sw_mem[i].to_string(16) 
                            << std::dec << "\n";
            }
            error_count++;
        }
    }
    if (error_count > max_errors) {
        std::cout << "[Mismatch] ...\n";
    }
        
    if (error_count > 0) {
        std::cout << "[COSIM] Verification FAILED! Total errors: " << error_count << "\n";
    } else {
        std::cout << "[COSIM] Verification Passed.\n";
    }
}

int main(int argc, char** argv) {
    Verilated::commandArgs(argc, argv);

    std::cout << "[COSIM] Starting Co-simulation..." << std::endl;

    SoftwareRenderer* sw_renderer = new SoftwareRenderer();
    HardwareRenderer* hw_renderer = new HardwareRenderer();
    ap_uint<128>* sw_vram_buffer = new ap_uint<128>[VRAM_SIZE];
    ap_uint<128>* hw_vram_buffer = new ap_uint<128>[VRAM_SIZE];

    memset(sw_vram_buffer, 0, VRAM_SIZE * sizeof(ap_uint<128>));
    memset(hw_vram_buffer, 0, VRAM_SIZE * sizeof(ap_uint<128>));

    int total_frames = 5;     
    for (int i = 0; i < total_frames; i++) {
        int angle = i * 15;
        int fb_id = 0;

        std::cout << "\n[COSIM]--- Processing Frame " << i << " (Angle: " << angle << ") ---" << std::endl;
        
        // Launch Software Renderer in a separate thread (Async)
        auto sw_future = std::async(std::launch::async, [&]() {
            sw_renderer->draw(fb_id, sw_vram_buffer, angle);
        });

        // Run Hardware Simulation in the main thread
        // (Verilator is not thread-safe, so it stays on the main thread)
        hw_renderer->draw(fb_id, hw_vram_buffer, angle);

        // Wait for Software Renderer to finish
        sw_future.get(); 

        // Compare Results
        compare_results(hw_vram_buffer, sw_vram_buffer);
        
        if (Verilated::gotFinish()) break;
    }

    delete sw_renderer, hw_renderer;
    delete[] sw_vram_buffer;
    delete[] hw_vram_buffer;

    std::cout << "\n[COSIM] Co-simulation Finished." << std::endl;
    return 0;
}