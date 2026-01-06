package fun.bm.serverresourcereloadonlinertm;

import fun.bm.serverresourcereloadonlinertm.command.SetServerResourceCommand;
import net.fabricmc.api.ModInitializer;

public class Serverresourcereloadonlinertm implements ModInitializer {

    @Override
    public void onInitialize() {
        SetServerResourceCommand.register();
    }
}
