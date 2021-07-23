package team.unnamed.hephaestus.model.animation;

import team.unnamed.hephaestus.model.ModelComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class that represents a model animation,
 * applied to the {@link ModelComponent} objects
 */
public class ModelAnimation {

    private final String name;

    /** Determines if the animation is infinite */
    private final boolean loop;

    /** Determines the animation length in ticks */
    private final int animationLength;

    /** Contains all the bone animations using the bone names as key */
    private final Map<String, ModelBoneAnimation> animationsByBoneName;

    private final Map<String, Map<Integer, Integer>> modelData = new HashMap<>();

    public ModelAnimation(
            String name,
            boolean loop,
            int animationLength,
            Map<String, ModelBoneAnimation> animationsByBoneName
    ) {
        this.name = name;
        this.loop = loop;
        this.animationLength = animationLength;
        this.animationsByBoneName = animationsByBoneName;
    }

    public Map<String, Map<Integer, Integer>> getModelData() {
        return modelData;
    }

    public String getName() {
        return name;
    }

    public boolean isLoop() {
        return loop;
    }

    public int getAnimationLength() {
        return animationLength;
    }

    public Map<String, ModelBoneAnimation> getAnimationsByBoneName() {
        return animationsByBoneName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelAnimation that = (ModelAnimation) o;
        return loop == that.loop && Float.compare(that.animationLength, animationLength) == 0 && Objects.equals(animationsByBoneName, that.animationsByBoneName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loop, animationLength, animationsByBoneName);
    }

    @Override
    public String toString() {
        return "ModelAnimation{" +
                "loop=" + loop +
                ", animationLength=" + animationLength +
                ", animationsByBoneName=" + animationsByBoneName +
                '}';
    }
}