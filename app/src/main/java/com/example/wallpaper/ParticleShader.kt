package com.example.wallpaper

object ParticleShader {
    val vertexShaderCode = """
        #version 300 es
        in vec4 vPosition;
        in vec4 vColor;
        in vec2 vSizeLife; // x = size, y = life
        
        uniform mat4 uMVPMatrix;
        
        out vec4 fColor;
        out float fLife;
        
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            gl_PointSize = vSizeLife.x;
            fColor = vColor;
            fLife = vSizeLife.y;
        }
    """.trimIndent()

    val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        
        in vec4 fColor;
        in float fLife;
        
        out vec4 fragColor;
        
        void main() {
            vec2 pt = gl_PointCoord - vec2(0.5);
            float dist = length(pt);
            if (dist > 0.5) discard;
            
            // Soft glow particle
            float alpha = (0.5 - dist) * 2.0;
            // Fade in/out based on life (0.0 to 1.0)
            float lifeAlpha = smoothstep(0.0, 0.2, fLife) * smoothstep(1.0, 0.8, fLife);
            
            fragColor = vec4(fColor.rgb, fColor.a * alpha * lifeAlpha);
        }
    """.trimIndent()
}
