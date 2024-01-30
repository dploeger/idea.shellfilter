package de.dieploegers.develop.idea.shellfilter.beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandBeanConverter extends Converter<CommandBean> {
    @Override
    public @Nullable CommandBean fromString(@NotNull final String s) {
        try {
            return new ObjectMapper().readValue(s, CommandBean.class);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable String toString(@NotNull final CommandBean commandBean) {
        try {
            return new ObjectMapper().writeValueAsString(commandBean);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
