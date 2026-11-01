package com.mestisexo.economymod.command;

import com.mestisexo.economymod.Config;
import com.mestisexo.economymod.economy.EconomyManager;
import com.mestisexo.economymod.economy.EconomyManager.TransferResult;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.UUID;

public final class EconomyCommands {
    private EconomyCommands() {}

    public static void register(RegisterCommandsEvent event) {
        var d = event.getDispatcher();

        d.register(Commands.literal("balance")
                .executes(EconomyCommands::balanceSelf)
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(EconomyCommands::balanceOther)));

        d.register(Commands.literal("pay")
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(EconomyCommands::pay))));

        d.register(Commands.literal("eco")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(EconomyCommands::give))))
                .then(Commands.literal("take")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(EconomyCommands::take))))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(EconomyCommands::set)))));
    }

    private static GameProfile single(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
        if (profiles.size() != 1) throw EntityArgument.ERROR_NOT_SINGLE_PLAYER.create();
        return profiles.iterator().next();
    }

    private static int balanceSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        long bal = EconomyManager.getBalance(src.getServer(), player.getUUID());
        src.sendSuccess(() -> Component.literal("Tu saldo: " + Config.format(bal)), false);
        return 1;
    }

    private static int balanceOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        GameProfile target = single(ctx);
        long bal = EconomyManager.getBalance(src.getServer(), target.getId());
        src.sendSuccess(() -> Component.literal("Saldo de " + target.getName() + ": " + Config.format(bal)), false);
        return 1;
    }

    private static int pay(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        MinecraftServer server = src.getServer();
        ServerPlayer payer = src.getPlayerOrException();
        GameProfile target = single(ctx);
        long amount = LongArgumentType.getLong(ctx, "amount");

        TransferResult result = EconomyManager.transfer(server, payer.getUUID(), target.getId(), amount);
        if (result != TransferResult.OK) {
            String msg = switch (result) {
                case SAME_PLAYER -> "No puedes pagarte a ti mismo.";
                case INVALID_AMOUNT -> "El monto debe ser mayor que 0.";
                case OVER_LIMIT -> "El máximo por pago es " + Config.format(Config.MAX_TRANSACTION.get()) + ".";
                case INSUFFICIENT_FUNDS -> "No tienes fondos suficientes.";
                case OVERFLOW -> "El saldo del destinatario no puede crecer más.";
                default -> "Error desconocido.";
            };
            src.sendFailure(Component.literal(msg));
            return 0;
        }

        src.sendSuccess(() -> Component.literal("Pagaste " + Config.format(amount) + " a " + target.getName() + "."), false);
        notifyIfOnline(server, target.getId(),
                payer.getGameProfile().getName() + " te pagó " + Config.format(amount) + ".");
        return 1;
    }

    private static int give(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        GameProfile target = single(ctx);
        long amount = LongArgumentType.getLong(ctx, "amount");
        if (!EconomyManager.deposit(src.getServer(), target.getId(), amount)) {
            src.sendFailure(Component.literal("No se pudo depositar (monto inválido o desbordamiento)."));
            return 0;
        }
        long bal = EconomyManager.getBalance(src.getServer(), target.getId());
        src.sendSuccess(() -> Component.literal("Diste " + Config.format(amount) + " a " + target.getName()
                + ". Nuevo saldo: " + Config.format(bal)), true);
        return 1;
    }

    private static int take(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        GameProfile target = single(ctx);
        long amount = LongArgumentType.getLong(ctx, "amount");
        if (!EconomyManager.withdraw(src.getServer(), target.getId(), amount)) {
            src.sendFailure(Component.literal(target.getName() + " no tiene fondos suficientes."));
            return 0;
        }
        long bal = EconomyManager.getBalance(src.getServer(), target.getId());
        src.sendSuccess(() -> Component.literal("Quitaste " + Config.format(amount) + " a " + target.getName()
                + ". Nuevo saldo: " + Config.format(bal)), true);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        GameProfile target = single(ctx);
        long amount = LongArgumentType.getLong(ctx, "amount");
        EconomyManager.setBalance(src.getServer(), target.getId(), amount);
        src.sendSuccess(() -> Component.literal("Saldo de " + target.getName() + " fijado en " + Config.format(amount) + "."), true);
        return 1;
    }

    private static void notifyIfOnline(MinecraftServer server, UUID id, String message) {
        ServerPlayer online = server.getPlayerList().getPlayer(id);
        if (online != null) online.sendSystemMessage(Component.literal(message));
    }
}
