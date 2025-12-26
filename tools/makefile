# Makefile for produce resourse for renderer

COMMON_DIR = ../verification/common
RESOURCES_DIR = ../soc/src/main/resources
SRCS_DATA = $(COMMON_DIR)/src/mesh.cpp $(COMMON_DIR)/src/texture.cpp $(COMMON_DIR)/src/math/math.cpp

INCLUDES = -I$(COMMON_DIR)/include -I$(COMMON_DIR)/include/types

CXX = g++
CXXFLAGS = -std=c++17 -O3 $(INCLUDES)

.PHONY: all clean

all: gen_hex

gen_hex : gen_hex.cpp
	mkdir -p $(RESOURCES_DIR)
	$(CXX) $(CXXFLAGS) $(SRCS_DATA) gen_hex.cpp -o gen_hex
	./gen_hex

clean:
	rm gen_hex
