# Chisel RasterI
> This project is based on [Raster I](https://github.com/raster-gpu/raster-i), licensed under the MIT License.
> All modifications are done by GitHub user **jin11109** © 2025.

## Quick Start

> [!IMPORTANT]
> This project is currently a **work in progress**. The repository is divided into two main parts: the **Chisel core logic** (defining RasterI) and the **Verification suite** (used for functional testing).

### Prerequisites

- **Verilator**: v5.020 or newer
- **OS**: Ubuntu 22.04 / 24.04

### Build and Simulate RasterI (Chisel)
1. firtool requirement:
    ```bash
    $ wget https://github.com/llvm/circt/releases/download/firtool-1.40.0/firrtl-bin-ubuntu-20.04.tar.gz
    # After extract firrtl-bin-ubuntu-20.04.tar.gz
    $ export PATH=<your-path>/firrtl-bin-ubuntu-20.04/firtool-1.40.0/bin:$PATH
    ```
2. Navigate to the SoC directory:
    ```bash
    cd soc
    ``` 
3. Run simulation:
    Ensure your Verilator version is **5.020** or newer. This process compiles the Chisel code into Verilog and runs a simulation using Verilator.

    The simulation executes a basic test bench that processes 4 frames, reads the framebuffer data directly, and compares it against expected values.
    > This is currently a temporary verification setup. The goal is to transition this to a visual simulation (screen display) and relocate the verification process to `cosim`.

    ```bash
    make sim
    ```

### C++ Simulation (Golden Reference)

1. Navigate to the verification directory:
    ```bash
    cd verification
    ```

2. Current Status:
    - **Cosimulation (`cosim`)**: Currently **unavailable** due to an ongoing refactor of the renderer.
    - **C-Simulation (`c_sim`)**: Fully **functional**. It operates independently of Chisel and is built entirely in C++.

3. Run C-Sim Demo:
    Execute the following command to view the golden reference result. The script will automatically download required libraries, compile the source, and display the output via SDL2.
    ```bash
    make csim
    ```
