package org.minerift.ether.config.readers;

import org.minerift.ether.config.ConfigFileView;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;
import org.minerift.ether.config.types.MainConfig;
import org.minerift.ether.util.Result;

public class MainConfigReader implements IConfigReader<MainConfig> {

    @Override
    public MainConfig read(ConfigType<MainConfig> type) throws ConfigFileReadException {

        final Result<MainConfig, ConfigFileReadException> result = new Result<>();

        // TODO
        ConfigFileView.from(type.getFile()).handle((view) -> {

            // Read islands as an example
            view.getSectionView("user.islands").ifPresent((section) -> {
                //section.get("");
            });

        },
        // Delegate ConfigFileView error to result
        (ex) -> result.err((ConfigFileReadException) ex));

        if(result.isErr()) {
            throw result.getErr();
        }

        return result.getOk();
    }
}
