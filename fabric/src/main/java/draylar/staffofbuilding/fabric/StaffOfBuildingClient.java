package draylar.staffofbuilding.fabric;

import draylar.staffofbuilding.fabric.network.ClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public class StaffOfBuildingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientNetworking.init();
    }
}
