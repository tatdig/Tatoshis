package me.meta1203.plugins.satoshis.commands;

import static me.meta1203.plugins.satoshis.commands.CommandUtil.action;
import static me.meta1203.plugins.satoshis.commands.CommandUtil.error;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.WrongNetworkException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.meta1203.plugins.satoshis.Satoshis;
import me.meta1203.plugins.satoshis.bitcoin.BitcoinAPI;
import me.meta1203.plugins.satoshis.cryptocoins.GenericAddress;

public class WithdrawCommand implements CommandExecutor {

    public boolean onCommand(CommandSender arg0, Command arg1, String arg2,
            String[] arg3) {
        if (!arg0.hasPermission("satoshis.withdraw")) {
            error("You do not have permission for this command!", arg0);
            return true;
        }

        if (arg0 instanceof Player) {
            Player player = (Player) arg0;

            // Withdraw exact amount
            if (arg3.length == 2) {
                try {
                    GenericAddress<?> withdrawTo = Satoshis.api.addressContainer();
                    withdrawTo.setAddress(arg3[0], Satoshis.api.getNetworkType());
                    
                    double withdraw = Double.parseDouble(arg3[1]);
                    if (!Satoshis.econ.hasMoney(player.getName(), Satoshis.minWithdraw)) {
                        error("Oops! You must have " + Satoshis.econ.formatValue(Satoshis.minWithdraw, false) + " to withdraw!", arg0);
                        return true;
                    }
                    if (!Satoshis.econ.hasMoney(player.getName(), withdraw)) {
                        error("Oops! You cannot withdraw more money than you have!", arg0);
                        return true;
                    }
                    if (!Satoshis.salesTax && !player.getName().equalsIgnoreCase(Satoshis.owner)) {
                        Satoshis.api.sendCoins(withdrawTo, withdraw - Satoshis.econ.priceOfTax(withdraw) - BitcoinAPI.minBitFee);
                    } else {
                        Satoshis.api.sendCoins(withdrawTo, withdraw - BitcoinAPI.minBitFee);
                    }
                    action("Sending " + Satoshis.econ.formatValue(withdraw - BitcoinAPI.minBitFee, false) + " to address " + withdrawTo.toString() + " sucessfully!", arg0);
                    Satoshis.econ.subFunds(player.getName(), withdraw + BitcoinAPI.minBitFee);
                } catch (WrongNetworkException e) {
                    error("Oops! That address was for the TestNet!", arg0);
                } catch (AddressFormatException e) {
                    error("Oops! Is that the correct address?", arg0);
                } catch (NumberFormatException e) {
                    error("Syntax: /withdraw <address> [amount]", arg0);
                    error("Amount must be a number!", arg0);
                }
            } else if (arg3.length == 1) {
                try {
                	GenericAddress<?> withdrawTo = Satoshis.api.addressContainer();
                    withdrawTo.setAddress(arg3[0], Satoshis.api.getNetworkType());
                    
                    double withdraw = Satoshis.econ.getMoney(player.getName());
                    if (withdraw == 0 + BitcoinAPI.minBitFee) {
                        error("Oops! You have no money in your account!", arg0);
                        return true;
                    }
                    if (!Satoshis.salesTax && !player.getName().equalsIgnoreCase(Satoshis.owner)) {
                        Satoshis.api.sendCoins(withdrawTo, withdraw - Satoshis.econ.priceOfTax(withdraw) - Satoshis.api.getMinimumFee());
                    } else {
                        Satoshis.api.sendCoins(withdrawTo, withdraw - Satoshis.api.getMinimumFee());
                    }
                    action("Sending " + Satoshis.econ.formatValue(withdraw - BitcoinAPI.minBitFee, false) + " to address " + withdrawTo.toString() + " sucessfully!", arg0);

                    Satoshis.econ.subFunds(player.getName(), withdraw);
                } catch (WrongNetworkException e) {
                    error("Oops! That address was for the TestNet!", arg0);
                } catch (AddressFormatException e) {
                    error("Oops! Is that the correct address?", arg0);
                } catch (NumberFormatException e) {
                    error("Syntax: /withdraw <address> [amount]", arg0);
                    error("Amount must be a number!", arg0);
                }
            } else {
                error("Syntax: /withdraw <address> [amount]", arg0);
            }
        }

        return true;
    }

}
