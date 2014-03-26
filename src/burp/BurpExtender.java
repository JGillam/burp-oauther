package burp;

public class BurpExtender implements IBurpExtender {

    private final com.professionallyevil.oather.OAutherExtension OAutherExtension = new com.professionallyevil.oather.OAutherExtension();

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {

        OAutherExtension.registerExtenderCallbacks(callbacks);

    }
}
