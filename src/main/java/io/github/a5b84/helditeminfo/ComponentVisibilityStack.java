package io.github.a5b84.helditeminfo;

public class ComponentVisibilityStack {

  private int depth = 0;

  public void pushHidden() {
    depth++;
  }

  public void popHidden() {
    depth--;
  }

  public boolean isHidden() {
    return depth > 0;
  }
}
