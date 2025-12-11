// Wraps existing HLS C++ function

#pragma once
#include "Renderer.hpp"
#include "fb.hpp"

void trinity_renderer(fb_id_t fb_id, ap_uint<128> *vram, ap_uint<9> angle);

class SoftwareRenderer : public Renderer {
public:
    void draw(fb_id_t fb_id, ap_uint<128>* vram, int angle) override {
        trinity_renderer(fb_id, vram, angle);
    }
};