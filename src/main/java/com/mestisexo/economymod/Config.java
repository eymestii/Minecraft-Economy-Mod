package com.mestisexo.economymod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.LongValue STARTING_BALANCE = BUILDER
            .comment("Saldo con el que empieza cada jugador la primera vez que se consulta")
            .defineInRange("startingBalance", 100L, 0L, 1_000_000_000L);

    public static final ModConfigSpec.LongValue MAX_TRANSACTION = BUILDER
            .comment("Monto máximo permitido por cada /pay")
            .defineInRange("maxTransaction", 1_000_000L, 1L, 1_000_000_000L);

    public static final ModConfigSpec.ConfigValue<String> CURRENCY_NAME = BUILDER
            .comment("Nombre de la moneda")
            .define("currencyName", "Sexos");

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static String format(long amount) {
        return amount + " " + CURRENCY_NAME.get();
    }
}
