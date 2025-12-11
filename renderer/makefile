# Generate Verilog and C++ simulation model
BUILD_DIR = generated
VERILATOR_DIR = verilator
OBJ_DIR = obj_dir
VERILATOR_FLAGS = --cc -j 4 --top-module Renderer -Mdir $(VERILATOR_DIR)/$(OBJ_DIR) -Wno-WIDTH

all: verilog verilate

verilog:
	sbt "runMain renderer.Renderer"

# Generate C++ model and compile it into a static library (.a)
verilate: verilog
	verilator $(VERILATOR_FLAGS) $(BUILD_DIR)/Renderer.sv
	make -C ./$(VERILATOR_DIR)/$(OBJ_DIR) -f VRenderer.mk

clean:
	rm -rf project target $(BUILD_DIR) $(VERILATOR_DIR)/$(OBJ_DIR)