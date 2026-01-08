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

package pers.ketikai.minecraft.spigot.betterattributeplus.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import team.idealstate.sugar.next.command.CommandContext;
import team.idealstate.sugar.next.command.annotation.CommandArgument.ConverterResult;
import team.idealstate.sugar.next.command.exception.CommandArgumentConversionException;
import team.idealstate.sugar.string.StringUtils;
import team.idealstate.sugar.validate.annotation.NotNull;

@SuppressWarnings("DuplicatedCode")
public abstract class CommandUtils {

    @NotNull
    public static ConverterResult<Player> convertToPlayer(
            @NotNull CommandContext context, @NotNull String argument, boolean onConversion, @NotNull Player... players)
            throws CommandArgumentConversionException {
        boolean canBeConvert = players.length > 0
                && Arrays.stream(players).map(Player::getName).anyMatch(s -> s.equals(argument));
        if (!onConversion) {
            return canBeConvert ? ConverterResult.success() : ConverterResult.failure();
        }
        if (!canBeConvert) {
            throw new CommandArgumentConversionException(String.format("参数 '%s' 无法转换到 player.", argument));
        }
        return ConverterResult.success(Bukkit.getPlayer(argument));
    }

    @NotNull
    public static List<String> completePlayer(
            @NotNull CommandContext context, @NotNull String argument, @NotNull Player... players) {
        if (argument.isEmpty()) {
            return Arrays.stream(players).map(Player::getName).collect(Collectors.toList());
        }
        return players.length == 0
                ? Collections.emptyList()
                : Arrays.stream(players)
                        .map(Player::getName)
                        .filter(s -> s.startsWith(argument))
                        .collect(Collectors.toList());
    }

    @NotNull
    public static ConverterResult<Long> convertToLong(
            @NotNull CommandContext context, @NotNull String argument, boolean onConversion)
            throws CommandArgumentConversionException {
        boolean canBeConvert = StringUtils.isInteger(argument);
        if (!onConversion) {
            if (canBeConvert) {
                try {
                    Long.parseLong(argument);
                } catch (NumberFormatException e) {
                    canBeConvert = false;
                }
            }
            return canBeConvert ? ConverterResult.success() : ConverterResult.failure();
        }
        if (!canBeConvert) {
            throw new CommandArgumentConversionException(String.format("参数 '%s' 无法转换到 long.", argument));
        }
        return ConverterResult.success(Long.parseLong(argument));
    }

    @NotNull
    public static List<String> completeLong(
            @NotNull CommandContext context, @NotNull String argument, long... integers) {
        if (argument.isEmpty()) {
            return Arrays.stream(integers).mapToObj(String::valueOf).collect(Collectors.toList());
        }
        return Arrays.stream(integers)
                .mapToObj(String::valueOf)
                .filter(s -> s.startsWith(argument))
                .collect(Collectors.toList());
    }

    private static final List<String> BOOLEAN = Collections.unmodifiableList(Arrays.asList("true", "false"));

    @NotNull
    public static ConverterResult<Boolean> convertToBoolean(
            @NotNull CommandContext context, @NotNull String argument, boolean onConversion)
            throws CommandArgumentConversionException {
        boolean canBeConvert = StringUtils.isBoolean(argument);
        if (!onConversion) {
            return canBeConvert ? ConverterResult.success() : ConverterResult.failure();
        }
        if (!canBeConvert) {
            throw new CommandArgumentConversionException(String.format("参数 '%s' 无法转换到 boolean.", argument));
        }
        return ConverterResult.success(Boolean.parseBoolean(argument));
    }

    @NotNull
    public static List<String> completeBoolean(@NotNull CommandContext context, @NotNull String argument) {
        if (argument.isEmpty()) {
            return BOOLEAN;
        }
        String lowerCase = argument.toLowerCase();
        return BOOLEAN.stream().filter(s -> s.startsWith(lowerCase)).collect(Collectors.toList());
    }
}
