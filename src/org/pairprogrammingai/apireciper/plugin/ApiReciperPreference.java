package org.pairprogrammingai.apireciper.plugin;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ApiReciperPreference implements SearchableConfigurable {

    private ApiReciperPreferenceGui gui;
    private final ApiRecipePreferenceConfig config;

    public ApiReciperPreference() {
        config = ApiRecipePreferenceConfig.getInstance();
    }

    @NotNull
    @Override
    public String getId() {
        return "preference.ApiReciperPreference";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "ApiReciper Preference";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        gui = new ApiReciperPreferenceGui(config);
        return gui.getRootPanel();
    }

    @Override
    public void disposeUIResources(){
        gui = null;
    }

    @Override
    public boolean isModified() {
        return gui.isModified(config);
    }

    @Override
    public void apply() throws ConfigurationException {
        gui.apply(config);
    }

    @Override
    public void reset(){
        gui.reset(config);
    }
}
