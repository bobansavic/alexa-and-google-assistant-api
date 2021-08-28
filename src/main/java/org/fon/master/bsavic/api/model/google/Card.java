package org.fon.master.bsavic.api.model.google;

public class Card {
    private String title, subtitle, text;
    private Image image;
    private ImageFill imageFill;
    private Link button;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public ImageFill getImageFill() {
        return imageFill;
    }

    public void setImageFill(ImageFill imageFill) {
        this.imageFill = imageFill;
    }

    public Link getButton() {
        return button;
    }

    public void setButton(Link button) {
        this.button = button;
    }
}
