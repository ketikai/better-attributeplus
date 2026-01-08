/*
 *    better-attributeplus
 *    Copyright (C) 2025  ketikai
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pers.ketikai.minecraft.spigot.betterattributeplus.controller;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.serverct.ersha.api.AttributeAPI;
import org.serverct.ersha.attribute.data.AttributeData;
import org.serverct.ersha.attribute.persistent.AttributePersistentData;
import org.serverct.ersha.attribute.persistent.AttributePersistentSource;
import pers.ketikai.minecraft.spigot.betterattributeplus.util.CommandUtils;
import team.idealstate.sugar.logging.Log;
import team.idealstate.sugar.next.command.Command;
import team.idealstate.sugar.next.command.CommandContext;
import team.idealstate.sugar.next.command.CommandResult;
import team.idealstate.sugar.next.command.annotation.CommandArgument;
import team.idealstate.sugar.next.command.annotation.CommandArgument.ConverterResult;
import team.idealstate.sugar.next.command.annotation.CommandHandler;
import team.idealstate.sugar.next.command.exception.CommandArgumentConversionException;
import team.idealstate.sugar.next.context.annotation.component.Controller;
import team.idealstate.sugar.next.context.lifecycle.Initializable;
import team.idealstate.sugar.validate.annotation.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Controller(name = "better-attributeplus")
public class BetterAttributePlusController implements Command, Initializable {

    private final FunctionHandler functionHandler = new FunctionHandler();
    private volatile MethodHandle modifySource = null;
    private volatile Object functionObject = null;

    @CommandHandler(value = "persistent {player} {source} {attributes} {date}")
    @NotNull
    public CommandResult persistent(
            @NotNull CommandContext context,
            @NotNull @CommandArgument(converter = "convertToPlayer", completer = "completePlayer") Player player,
            @CommandArgument String source,
            @CommandArgument String attributes,
            @CommandArgument(converter = "convertToLong", completer = "completeLong") Long date
    ) {
        return persistent(context, player, source, attributes, date, true);
    }

    @CommandHandler(value = "persistent {player} {source} {attributes} {date} {save}")
    @NotNull
    public CommandResult persistent(
            @NotNull CommandContext context,
            @NotNull @CommandArgument(converter = "convertToPlayer", completer = "completePlayer") Player player,
            @CommandArgument String source,
            @CommandArgument String attributes,
            @CommandArgument(converter = "convertToLong", completer = "completeLong") Long date,
            @CommandArgument(converter = "convertToBoolean", completer = "completeBoolean") Boolean save
    ) {
        try {
            AttributeData attrData = AttributeAPI.getAttrData(player);
            AttributePersistentData persistent = attrData.getPersistent();
            modifySource(persistent, source, save, ps -> {
                ps.setDate(date);
                for (String attribute : attributes.split(",")) {
                    ps.addAttribute(attribute);
                }
            });
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(
                    String.format("未能推送属性源修改操作：(%s, %s, %s, %s, %s)，错误信息请查看日志输出。", player.getName(), source, attributes, date, save));
        }
        return CommandResult.success("已推送属性源修改操作");
    }

    public void modifySource(AttributePersistentData persistentData, String source, boolean save, Consumer<AttributePersistentSource> consumer) throws Throwable {
        if (persistentData == null) {
            return;
        }
        functionHandler.setLocal(consumer);
        modifySource.bindTo(persistentData).invokeWithArguments(source, save, functionObject);
    }

    @Override
    public void initialize() {
        Method[] methods = AttributePersistentData.class.getMethods();
        for (Method method : methods) {
            if ("modifySource".equals(method.getName())) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 3) {
                    try {
                        this.modifySource = MethodHandles.publicLookup().unreflect(method);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    this.functionObject = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{parameterTypes[2]}, functionHandler);
                    break;
                }
            }
        }
        if (functionObject == null) {
            throw new RuntimeException("未找到 modifySource 方法，不兼容的 AP 版本！");
        }
    }

    @NotNull
    public ConverterResult<Player> convertToPlayer(
            @NotNull CommandContext context, @NotNull String argument, boolean onConversion)
            throws CommandArgumentConversionException {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) {
            return CommandUtils.convertToPlayer(context, argument, onConversion);
        }
        return CommandUtils.convertToPlayer(context, argument, onConversion, players.toArray(new Player[0]));
    }

    @NotNull
    public List<String> completePlayer(@NotNull CommandContext context, @NotNull String argument) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) {
            return CommandUtils.completePlayer(context, argument);
        }
        return CommandUtils.completePlayer(context, argument, players.toArray(new Player[0]));
    }

    @NotNull
    public ConverterResult<Long> convertToLong(
            @NotNull CommandContext context, @NotNull String argument, boolean onConversion)
            throws CommandArgumentConversionException {
        return CommandUtils.convertToLong(context, argument, onConversion);
    }

    @NotNull
    public List<String> completeLong(@NotNull CommandContext context, @NotNull String argument) {
        return CommandUtils.completeLong(context, argument);
    }

    @NotNull
    public ConverterResult<Boolean> convertToBoolean(
            @NotNull CommandContext context, @NotNull String argument, boolean onConversion)
            throws CommandArgumentConversionException {
        return CommandUtils.convertToBoolean(context, argument, onConversion);
    }

    @NotNull
    public List<String> completeBoolean(@NotNull CommandContext context, @NotNull String argument) {
        return CommandUtils.completeBoolean(context, argument);
    }

    private static final class FunctionHandler implements InvocationHandler {

        private final ThreadLocal<Consumer<AttributePersistentSource>> local = ThreadLocal.withInitial(() -> null);

        public void setLocal(Consumer<AttributePersistentSource> consumer) {
            local.set(consumer);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("invoke")) {
                Consumer<AttributePersistentSource> consumer = local.get();
                if (consumer != null) {
                    try {
                        AttributePersistentSource source = (AttributePersistentSource) args[0];
                        consumer.accept(source);
                    } catch (Throwable e) {
                        Log.error(e);
                    }
                }
                local.remove();
                return null;
            }
            throw new UnsupportedOperationException();
        }
    }
}
