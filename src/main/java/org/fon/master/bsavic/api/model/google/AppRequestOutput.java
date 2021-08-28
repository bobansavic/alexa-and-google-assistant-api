package org.fon.master.bsavic.api.model.google;

public class AppRequestOutput {
    private Prompt prompt;
    private Scene scene;
    private Session session;
    private User user;
    private Home home;
    private Device device;
    private Expected expected;

    public Prompt getPrompt() {
        return prompt;
    }

    public void setPrompt(Prompt prompt) {
        this.prompt = prompt;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Home getHome() {
        return home;
    }

    public void setHome(Home home) {
        this.home = home;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Expected getExpected() {
        return expected;
    }

    public void setExpected(Expected expected) {
        this.expected = expected;
    }
}
