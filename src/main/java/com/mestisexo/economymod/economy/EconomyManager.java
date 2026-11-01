package com.mestisexo.economymod.economy;

import com.mestisexo.economymod.Config;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public final class EconomyManager {
    private EconomyManager() {}

    public enum TransferResult {
        OK, SAME_PLAYER, INVALID_AMOUNT, OVER_LIMIT, INSUFFICIENT_FUNDS, OVERFLOW
    }

    private static EconomyData data(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(EconomyData.FACTORY, "economymod_balances");
    }

    private static long balanceOf(EconomyData d, UUID id) {
        Long bal = d.get(id);
        if (bal == null) {
            bal = Config.STARTING_BALANCE.get();
            d.set(id, bal);
        }
        return bal;
    }

    public static long getBalance(MinecraftServer server, UUID id) {
        return balanceOf(data(server), id);
    }

    public static boolean deposit(MinecraftServer server, UUID id, long amount) {
        if (amount <= 0) return false;
        EconomyData d = data(server);
        long bal = balanceOf(d, id);
        if (bal > Long.MAX_VALUE - amount) return false;
        d.set(id, bal + amount);
        return true;
    }

    public static boolean withdraw(MinecraftServer server, UUID id, long amount) {
        if (amount <= 0) return false;
        EconomyData d = data(server);
        long bal = balanceOf(d, id);
        if (bal < amount) return false;
        d.set(id, bal - amount);
        return true;
    }

    public static boolean setBalance(MinecraftServer server, UUID id, long amount) {
        if (amount < 0) return false;
        data(server).set(id, amount);
        return true;
    }

    public static TransferResult transfer(MinecraftServer server, UUID from, UUID to, long amount) {
        if (from.equals(to)) return TransferResult.SAME_PLAYER;
        if (amount <= 0) return TransferResult.INVALID_AMOUNT;
        if (amount > Config.MAX_TRANSACTION.get()) return TransferResult.OVER_LIMIT;

        EconomyData d = data(server);
        long fromBal = balanceOf(d, from);
        long toBal = balanceOf(d, to);
        if (fromBal < amount) return TransferResult.INSUFFICIENT_FUNDS;
        if (toBal > Long.MAX_VALUE - amount) return TransferResult.OVERFLOW;

        d.set(from, fromBal - amount);
        d.set(to, toBal + amount);
        return TransferResult.OK;
    }
}
