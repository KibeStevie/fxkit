package dev.fxkit.core.components;

import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * A row of overlapping avatars (Flowbite's {@code <AvatarGroup>}). Children overlap by 16px, later
 * children on top. Put {@link FxAvatar}s with {@code stacked = true} in it, optionally ending with an
 * {@link FxAvatarGroupCounter}.
 *
 * <pre>{@code
 * FxAvatar a = new FxAvatar("/images/people/profile-picture-1.jpg");
 * a.setRounded(true);
 * a.setStacked(true);
 * FxAvatarGroup group = new FxAvatarGroup(a, new FxAvatarGroupCounter(99));
 * }</pre>
 */
public class FxAvatarGroup extends HBox {

    public FxAvatarGroup(Node... avatars) {
        getStyleClass().add("fxk-avatar-group");
        getChildren().addAll(avatars);
    }
}
