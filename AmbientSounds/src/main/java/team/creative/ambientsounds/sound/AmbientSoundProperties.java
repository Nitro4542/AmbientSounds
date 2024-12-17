package team.creative.ambientsounds.sound;

import com.google.gson.annotations.SerializedName;

import net.minecraft.util.Mth;
import team.creative.ambientsounds.condition.AmbientCondition.AmbientMinMaxCondition;
import team.creative.ambientsounds.engine.AmbientEngine;
import team.creative.ambientsounds.engine.AmbientEngineLoadException;
import team.creative.ambientsounds.environment.AmbientEnvironment;

public class AmbientSoundProperties {
    
    @SerializedName("transition")
    public Integer transition;
    
    public Double pitch = 1D;
    
    @SerializedName("fade-volume")
    public Double fadeVolume;
    @SerializedName("fade-in-volume")
    public Double fadeInVolume;
    @SerializedName("fade-out-volume")
    public Double fadeOutVolume;
    
    @SerializedName("fade-pitch")
    public Double fadePitch;
    @SerializedName("fade-in-pitch")
    public Double fadeInPitch;
    @SerializedName("fade-out-pitch")
    public Double fadeOutPitch;
    
    public Double mute;
    @SerializedName("mute-priority")
    public Double mutePriority;
    
    @SerializedName("random-offset")
    public boolean randomOffset = true;
    
    public AmbientMinMaxCondition pause;
    public AmbientMinMaxCondition length;
    
    @SerializedName("underwater-pitch")
    public AmbientMinMaxClimbingProperty underwaterPitch;
    
    public String channel;
    
    public void init(AmbientEngine engine) throws AmbientEngineLoadException {
        if (mute != null)
            mute = Mth.clamp(mute, 0, 1);
    }
    
    public double getFadeInVolume(AmbientEngine engine) {
        if (fadeInVolume != null)
            return fadeInVolume;
        if (fadeVolume != null)
            return fadeVolume;
        return engine.fadeVolume;
    }
    
    public double getFadeOutVolume(AmbientEngine engine) {
        if (fadeOutVolume != null)
            return fadeOutVolume;
        if (fadeVolume != null)
            return fadeVolume;
        return engine.fadeVolume;
    }
    
    public double getFadeInPitch(AmbientEngine engine) {
        if (fadeInPitch != null)
            return fadeInPitch;
        if (fadePitch != null)
            return fadePitch;
        return engine.fadePitch;
    }
    
    public double getFadeOutPitch(AmbientEngine engine) {
        if (fadeOutPitch != null)
            return fadeOutPitch;
        if (fadePitch != null)
            return fadePitch;
        return engine.fadePitch;
    }
    
    public float getPitch(AmbientEnvironment env) {
        if (underwaterPitch != null)
            return (pitch != null ? (float) (double) pitch : 1) + (float) underwaterPitch.getValue(env.underwater);
        return pitch != null ? (float) (double) pitch : 1;
    }
    
    public static class AmbientMinMaxClimbingProperty {
        
        public double min = 0;
        public double max;
        @SerializedName("distance-factor")
        public double distanceFactor = 1;
        
        public double getValue(double value) {
            if (max <= min)
                max = min + 1;
            double distance = max - min;
            return value / distance * distanceFactor;
        }
        
    }
    
}
