package com.example.wallpaper

object ChargingRingShader {
    val vertexShaderCode = """
        #version 300 es
        in vec4 vPosition;
        in vec2 vTexCoord;
        uniform mat4 uMVPMatrix;
        out vec2 fTexCoord;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            fTexCoord = vTexCoord;
        }
    """.trimIndent()

    val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        in vec2 fTexCoord;
        uniform float uTime;
        uniform float uAlpha;
        out vec4 fragColor;
        void main() {
            vec2 uv = fTexCoord * 2.0 - 1.0;
            float r = length(uv);
            float angle = atan(uv.y, uv.x);
            
            float ring = smoothstep(0.6, 0.5, r) - smoothstep(0.5, 0.4, r);
            float glow = exp(-8.0 * abs(r - 0.5));
            
            float energy = sin(angle * 8.0 - uTime * 5.0) * 0.5 + 0.5;
            float pulse = sin(uTime * 3.0) * 0.2 + 0.8;
            
            vec3 color = vec3(0.2, 1.0, 0.4) * energy * pulse + vec3(0.0, 0.5, 0.2);
            
            fragColor = vec4(color * (ring + glow) * uAlpha, (ring + glow) * uAlpha);
        }
    """.trimIndent()
}
