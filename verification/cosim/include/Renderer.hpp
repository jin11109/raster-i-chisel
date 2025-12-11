// Interface for all renderers

#pragma once
#include "ap_int.h"
#include "fb.hpp"

class Renderer {
public:
    virtual ~Renderer() = default;

    virtual void draw(fb_id_t fb_id, ap_uint<128>* vram, int angle) = 0;
};