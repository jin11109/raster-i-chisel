# Makefile for C(software, golden reference)/Chisel(hardware) co-simulation

HW_DIR  = ../../soc/verilator/obj_dir
SRC_DIR = src
OUT_DIR = build
EXE = cosim
COMMON_DIR = ../common

VERILATOR_ROOT = /usr/local/share/verilator/
VERILATED_CPP  = $(VERILATOR_ROOT)/include/verilated.cpp \
                 $(VERILATOR_ROOT)/include/verilated_vcd_c.cpp \
                 $(VERILATOR_ROOT)/include/verilated_threads.cpp

INCLUDES = -I./include \
           -I$(COMMON_DIR)/include -I$(COMMON_DIR)/include/types \
           -I$(VERILATOR_ROOT)/include -I$(VERILATOR_ROOT)/include/vltstd \
           -I$(HW_DIR)

SRC_SW_DATA = $(COMMON_DIR)/src/math/math.cpp $(COMMON_DIR)/src/mesh.cpp $(COMMON_DIR)/src/texture.cpp
SRCS = src/main.cpp $(VERILATED_CPP) $(SRC_SW_DATA)

HW_LIB = $(HW_DIR)/VTrinity__ALL.a

CXX = g++
CXXFLAGS = -std=c++17 -O3 -pthread $(INCLUDES) -DUSE_VERILATOR -Wno-unknown-pragmas

.PHONY: all clean

all: $(OUT_DIR)/$(EXE)

$(OUT_DIR)/$(EXE): $(SRCS) $(HW_LIB)
	@mkdir -p $(OUT_DIR)
	$(CXX) $(CXXFLAGS) $(SRCS) $(HW_LIB) -o $(OUT_DIR)/$(EXE)

cosim: $(OUT_DIR)/$(EXE)
	$(OUT_DIR)/$(EXE)
	@echo "[INFO] To view waveform: gtkwave ./cosim/trace.vcd"

clean:
	rm -rf $(OUT_DIR)
