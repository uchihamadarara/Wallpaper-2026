package com.example.wallpaper

import android.opengl.GLES30

object EarthShader {
    val vertexShaderCode = """
        #version 300 es
        in vec4 vPosition;
        in vec3 vNormal;
        in vec2 vTexCoord;
        
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;
        
        out vec3 fNormal;
        out vec3 fFragPos;
        out vec2 fTexCoord;
        
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            fFragPos = vec3(uModelMatrix * vPosition);
            fNormal = mat3(transpose(inverse(uModelMatrix))) * vNormal;
            fTexCoord = vTexCoord;
        }
    """.trimIndent()

    val fragmentShaderCode = """
        #version 300 es
        precision highp float;
        
        in vec3 fNormal;
        in vec3 fFragPos;
        in vec2 fTexCoord;
        
        uniform sampler2D uDayTexture;
        uniform sampler2D uNightTexture;
        uniform sampler2D uCloudTexture;
        
        uniform vec3 uLightPos;
        uniform vec3 uViewPos;
        
        uniform int uHasDayTex;
        uniform int uHasNightTex;
        uniform int uHasCloudTex;
        
        out vec4 fragColor;
        
        void main() {
            vec3 norm = normalize(fNormal);
            vec3 lightDir = normalize(uLightPos - fFragPos);
            vec3 viewDir = normalize(uViewPos - fFragPos);
            
            // Diffuse
            float diff = max(dot(norm, lightDir), 0.0);
            
            // Textures
            vec3 dayColor = vec3(0.0, 0.3, 0.7); // Fallback ocean blue
            if (uHasDayTex == 1) {
                dayColor = texture(uDayTexture, fTexCoord).rgb;
            }
            
            vec3 nightColor = vec3(0.0);
            if (uHasNightTex == 1) {
                nightColor = texture(uNightTexture, fTexCoord).rgb;
                // Boost night lights
                nightColor *= 2.0;
            }
            
            float cloudAlpha = 0.0;
            if (uHasCloudTex == 1) {
                // simple cloud animation offset based on time could be passed in, but static for now
                vec4 cloudSample = texture(uCloudTexture, fTexCoord);
                cloudAlpha = cloudSample.r; // assuming greyscale cloud map
            }
            
            // Lighting mix
            float dayMix = smoothstep(-0.2, 0.2, dot(norm, lightDir)); // Smooth day/night terminator
            
            // Base earth color
            vec3 earthColor = mix(nightColor, dayColor * diff, dayMix);
            
            // Add clouds
            vec3 cloudColor = vec3(1.0) * diff; // Clouds are white and lit by sun
            // Cloud shadows slightly
            earthColor = mix(earthColor, earthColor * 0.5, cloudAlpha * 0.5);
            // Blend clouds over earth
            vec3 finalColor = mix(earthColor, cloudColor, cloudAlpha * dayMix);
            
            // Atmospheric scattering (simple rim light)
            float rim = 1.0 - max(dot(viewDir, norm), 0.0);
            rim = smoothstep(0.6, 1.0, rim);
            vec3 atmosphereColor = vec3(0.3, 0.6, 1.0) * rim * dayMix;
            
            finalColor += atmosphereColor;
            
            // HDR tonemapping & gamma correction
            finalColor = finalColor / (finalColor + vec3(1.0));
            finalColor = pow(finalColor, vec3(1.0 / 2.2));
            
            fragColor = vec4(finalColor, 1.0);
        }
    """.trimIndent()
}
