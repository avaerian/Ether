package org.minerift.ether.config.readers;

import org.minerift.ether.config.ConfigSectionView;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;
import org.minerift.ether.config.types.MainConfig;
import org.minerift.ether.util.Result;

import java.io.FileNotFoundException;

public class MainConfigReader extends IConfigReader<MainConfig> {

    @Override
    public MainConfig read(ConfigType<MainConfig> type) throws ConfigFileReadException, FileNotFoundException {

        ensureFileExists(type);

        final Result<MainConfig, ConfigFileReadException> result = new Result<>();

        // TODO
        ConfigSectionView.from(type.getFile()).handle((view) -> {

            // Read islands as an example
            view.getSectionView("user.islands").ifPresent((section) -> {
                //section.get("");
            });

        },
        // Delegate ConfigSectionView error to result
        (ex) -> result.err((ConfigFileReadException) ex));

        if(result.isErr()) {
            throw result.getErr();
        }

        return result.getOk();
    }
}
