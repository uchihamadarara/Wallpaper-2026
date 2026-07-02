package com.example.wallpaper

object LayerShader {
    val vertexShaderCode = """
        #version 300 es
        in vec4 vPosition;
        in vec2 vTexCoord;
        
        uniform mat4 uMVPMatrix;
        uniform vec2 uOffset; // Parallax offset
        uniform float uScale; // Layer scale (for depth scaling)
        uniform vec2 uScroll; // Scrolling offset (e.g. for fog)
        
        out vec2 fTexCoord;
        
        void main() {
            vec4 pos = vPosition;
            pos.xy *= uScale;
            pos.xy += uOffset;
            gl_Position = uMVPMatrix * pos;
            fTexCoord = vTexCoord + uScroll;
        }
    """.trimIndent()

    val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        
        in vec2 fTexCoord;
        uniform sampler2D uTexture;
        uniform float uAlpha;
        uniform vec3 uColorTint;
        uniform float uBlur;
        
        out vec4 fragColor;
        
        void main() {
            vec4 texColor = texture(uTexture, fTexCoord);
            if (uBlur > 0.0) {
                float offset = uBlur * 0.005;
                texColor += texture(uTexture, fTexCoord + vec2(offset, 0.0));
                texColor += texture(uTexture, fTexCoord + vec2(-offset, 0.0));
                texColor += texture(uTexture, fTexCoord + vec2(0.0, offset));
                texColor += texture(uTexture, fTexCoord + vec2(0.0, -offset));
                texColor += texture(uTexture, fTexCoord + vec2(offset, offset));
                texColor += texture(uTexture, fTexCoord + vec2(-offset, offset));
                texColor += texture(uTexture, fTexCoord + vec2(offset, -offset));
                texColor += texture(uTexture, fTexCoord + vec2(-offset, -offset));
                texColor /= 9.0;
            }
            
            texColor.rgb *= uColorTint;
            fragColor = vec4(texColor.rgb, texColor.a * uAlpha);
        }
    """.trimIndent()
}
