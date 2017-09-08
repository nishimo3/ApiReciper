package org.pairprogrammingai.apireciper.plugin;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(
        name="ApiRecipePreferenceConfig",
        storages = {
                @Storage("ApiRecipePreferenceConfig.xml")}
)
public class ApiRecipePreferenceConfig implements PersistentStateComponent<ApiRecipePreferenceConfig> {
    private String androidSdkPath = "";
    private String indexPath = "";
    private String androidSdkVersion = "";
    private boolean isSupportPPAI = false;

    public String getAndroidSdkPath() {
        return androidSdkPath;
    }
    public String getIndexPath() {
        return indexPath;
    }
    public String getAndroidSdkVersion() {
        return androidSdkVersion;
    }
    public boolean getIsSupportPPAI(){
        return isSupportPPAI;
    }

    public void setAndroidSdkPath(String androidSdkPath) {
        this.androidSdkPath = androidSdkPath;
    }
    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }
    public void setAndroidSdkVersion(String androidSdkVersion) {
        this.androidSdkVersion = androidSdkVersion;
    }
    public void setIsSupportPPAI(boolean isSupportPPAI){
        this.isSupportPPAI = isSupportPPAI;
    }

    @Nullable
    @Override
    public ApiRecipePreferenceConfig getState() {
        return this;
    }

    @Override
    public void loadState(ApiRecipePreferenceConfig apiRecipePreferenceConfig) {
        XmlSerializerUtil.copyBean(apiRecipePreferenceConfig, this);
    }

    @Nullable
    public static ApiRecipePreferenceConfig getInstance() {
        return ServiceManager.getService(ApiRecipePreferenceConfig.class);
    }

    public boolean isUseful(){
        if(!androidSdkPath.equals("") && !indexPath.equals("") && !androidSdkVersion.equals("")){
            return true;
        }
        return false;
    }
}
