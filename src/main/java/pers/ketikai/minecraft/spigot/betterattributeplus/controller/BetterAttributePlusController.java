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
import team.idealstate.sugar.validate.annotation.NotNull;

import java.util.Collection;
import java.util.List;

@Controller(name = "better-attributeplus")
public class BetterAttributePlusController implements Command {

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
            if (persistent != null) {
                persistent.modifySource(source, save, ps -> {
                    ps.setDate(date);
                    for (String attribute : attributes.split(",")) {
                        ps.addAttribute(attribute);
                    }
                    return null;
                });
            }
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(
                    String.format("未能推送属性源修改操作：(%s, %s, %s, %s, %s)，错误信息请查看日志输出。", player.getName(), source, attributes, date, save));
        }
        return CommandResult.success("已推送属性源修改操作");
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
}
