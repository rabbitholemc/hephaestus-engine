package team.unnamed.hephaestus.adapt;

import team.unnamed.hephaestus.model.view.ModelViewAnimator;
import team.unnamed.hephaestus.model.view.ModelViewRenderer;

public interface AdaptionModule {

    ModelViewRenderer createRenderer(ModelViewAnimator animator);

}
