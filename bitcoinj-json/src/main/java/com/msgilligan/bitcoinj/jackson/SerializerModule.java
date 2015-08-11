package com.msgilligan.bitcoinj.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ser.Serializers;

/**
 *
 */
public class SerializerModule extends Module {
    @Override
    public String getModuleName() {
        return "BitcoinJModule";
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, null, "com.msgilligan.bitcoinj", "jackson");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new BitcoinJSerializers());
    }
}
