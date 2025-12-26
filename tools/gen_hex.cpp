#include <iostream>
#include <fstream>
#include <iomanip>
#include <vector>
#include <string>

#include "ap_fixed.h"
#include "ap_int.h"
#include "math/vec.hpp"
#include "utils/color.hpp"

typedef ap_fixed<24, 13> FixedType;
typedef ap_uint<72> PackedVertex; 
typedef ap_uint<48> PackedIndex;  
typedef ap_uint<32> PackedColor; 

#include "mesh.hpp"
#include "texture.hpp"
#include "math/math.hpp"

const char* VERTEX_OUT  = "../soc/src/main/resources/vertices.hex";
const char* NORMAL_OUT  = "../soc/src/main/resources/normals.hex";
const char* INDEX_OUT   = "../soc/src/main/resources/indices.hex";
const char* TEXTURE_OUT = "../soc/src/main/resources/texture.hex";
const char* SINE_OUT    = "../soc/src/main/resources/sine.hex";
const char* COSINE_OUT  = "../soc/src/main/resources/cosine.hex";

std::string format_hex(std::string raw, int width) {
    if (raw.substr(0, 2) == "0x" || raw.substr(0, 2) == "0X") {
        raw = raw.substr(2);
    }
    
    if (raw.length() < width) {
        return std::string(width - raw.length(), '0') + raw;
    }
    return raw;
}

void write_vertices() {
    std::ofstream out(VERTEX_OUT);
    if (!out.is_open()) { std::cerr << "Error opening " << VERTEX_OUT << std::endl; return; }
    std::cout << "Writing Vertices..." << std::endl;

    for (int i = 0; i < NR_MESH_VERTICES; ++i) {
        PackedVertex packed = 0;
        packed.range(23, 0)  = FixedType(MESH_VERTICES[i].x).range(23, 0);
        packed.range(47, 24) = FixedType(MESH_VERTICES[i].y).range(23, 0);
        packed.range(71, 48) = FixedType(MESH_VERTICES[i].z).range(23, 0);

        // 72 bits = 18 hex digits
        out << format_hex(packed.to_string(16), 18) << "\n";
    }
}

void write_normals() {
    std::ofstream out(NORMAL_OUT);
    if (!out.is_open()) { std::cerr << "Error opening " << NORMAL_OUT << std::endl; return; }
    std::cout << "Writing Normals..." << std::endl;

    for (int i = 0; i < NR_MESH_NORMALS; ++i) {
        PackedVertex packed = 0;
        packed.range(23, 0)  = FixedType(MESH_NORMALS[i].x).range(23, 0);
        packed.range(47, 24) = FixedType(MESH_NORMALS[i].y).range(23, 0);
        packed.range(71, 48) = FixedType(MESH_NORMALS[i].z).range(23, 0);

        // 72 bits = 18 hex digits
        out << format_hex(packed.to_string(16), 18) << "\n";
    }
}

void write_indices() {
    std::ofstream out(INDEX_OUT);
    if (!out.is_open()) { std::cerr << "Error opening " << INDEX_OUT << std::endl; return; }
    std::cout << "Writing Indices..." << std::endl;

    for (int i = 0; i < NR_MESH_TRIANGLES; ++i) {
        PackedIndex packed = 0;
        packed.range(15, 0)  = ap_uint<16>(MESH_INDICES[i].vertices.x);
        packed.range(31, 16) = ap_uint<16>(MESH_INDICES[i].vertices.y);
        packed.range(47, 32) = ap_uint<16>(MESH_INDICES[i].vertices.z);

        // 48 bits = 12 hex digits
        out << format_hex(packed.to_string(16), 12) << "\n";
    }
}

void write_texture() {
    std::ofstream out(TEXTURE_OUT);
    if (!out.is_open()) { std::cerr << "Error opening " << TEXTURE_OUT << std::endl; return; }
    std::cout << "Writing Texture..." << std::endl;

    int size = sizeof(TEXTURE) / sizeof(TEXTURE[0]);
    for (int i = 0; i < size; ++i) {
        RGB8 color = RGB8::decode(TEXTURE[i]);
        
        PackedColor packed = 0;
        packed.range(7, 0)   = color.b;
        packed.range(15, 8)  = color.g;
        packed.range(23, 16) = color.r;
        packed.range(31, 24) = 0; 

        // 32 bits = 8 hex digits
        out << format_hex(packed.to_string(16), 8) << "\n";
    }
}

void write_math_tables() {
    std::ofstream out_sin(SINE_OUT);
    std::ofstream out_cos(COSINE_OUT);
    if (!out_sin.is_open() || !out_cos.is_open()) { std::cerr << "Error opening math tables" << std::endl; return; }
    std::cout << "Writing Math Tables..." << std::endl;

    int size = sizeof(SINE_TABLE) / sizeof(SINE_TABLE[0]);
    for (int i = 0; i < size; ++i) {
        FixedType s = SINE_TABLE[i];
        FixedType c = COSINE_TABLE[i];
        
        ap_uint<24> raw_s = s.range(23, 0);
        ap_uint<24> raw_c = c.range(23, 0);

        // 24 bits = 6 hex digits
        out_sin << format_hex(raw_s.to_string(16), 6) << "\n";
        out_cos << format_hex(raw_c.to_string(16), 6) << "\n";
    }
}

int main() {
    write_vertices();
    write_normals();
    write_indices();
    write_texture();
    write_math_tables();
    std::cout << "Done! All hex files generated." << std::endl;
    return 0;
}