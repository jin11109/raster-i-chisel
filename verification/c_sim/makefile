# Makefile for simulation

# Build configuration
SRC_DIR := src
OUT_DIR := build
COMMON_DIR := ../common
EXE := sim

SRC_TOP = $(COMMON_DIR)/src/top.cpp
SRC_DATA = $(COMMON_DIR)/src/math/math.cpp $(COMMON_DIR)/src/mesh.cpp $(COMMON_DIR)/src/texture.cpp
# Find all cpp files recursively
SRCS := $(shell find $(SRC_DIR) -name "*.cpp") $(SRC_TOP) $(SRC_DATA)
OBJS := $(patsubst $(SRC_DIR)/%.cpp, $(OUT_DIR)/%.o, $(SRCS))

INCLUDES = -I$(COMMON_DIR)/include -I$(COMMON_DIR)/include/types
CXX = g++
CXXFLAGS = -std=c++17 -Iinclude $(INCLUDES) -Wno-unknown-pragmas -g
LDFLAGS = -lSDL2

.PHONY: all clean lib demo

all: $(OUT_DIR)/$(EXE)

demo: $(OUT_DIR)/$(EXE)
	$(OUT_DIR)/$(EXE)

$(OUT_DIR)/%.o: $(SRC_DIR)/%.cpp
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) -c $< -o $@

$(OUT_DIR)/$(EXE): $(OBJS)
	@mkdir -p $(OUT_DIR)
	$(CXX) $(CXXFLAGS) $^ -o $@ $(LDFLAGS)
	@echo "[INFO] Executable generated at $(OUT_DIR)/$(EXE)"

$(OUT_DIR)/$(LIB_NAME): $(LIB_OBJS)
	@mkdir -p $(OUT_DIR)
	ar rcs $@ $^
	@echo "[INFO] Static library generated at $@"

clean:
	@rm -rf $(OUT_DIR)
