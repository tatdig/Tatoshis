package me.meta1203.plugins.satoshis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import com.google.common.util.concurrent.Futures;

import me.meta1203.plugins.satoshis.bitcoin.BitcoinAPI;
import me.meta1203.plugins.satoshis.bitcoin.CoinListener;
import me.meta1203.plugins.satoshis.bitcoin.TransactionListener;
import me.meta1203.plugins.satoshis.commands.AdminCommand;
import me.meta1203.plugins.satoshis.commands.CheckCommand;
import me.meta1203.plugins.satoshis.commands.CreditCommand;
import me.meta1203.plugins.satoshis.commands.DebitCommand;
import me.meta1203.plugins.satoshis.commands.DepositCommand;
import me.meta1203.plugins.satoshis.commands.MoneyCommand;
import me.meta1203.plugins.satoshis.commands.SendCommand;
import me.meta1203.plugins.satoshis.commands.WithdrawCommand;
import me.meta1203.plugins.satoshis.cryptocoins.CryptocoinAPI;
import me.meta1203.plugins.satoshis.cryptocoins.NetworkType;
import me.meta1203.plugins.satoshis.database.DatabaseScanner;
import me.meta1203.plugins.satoshis.database.SystemCheckThread;
import net.milkbowl.vault.economy.Economy;

public class Satoshis extends JavaPlugin implements Listener {

    // Plugin
	public static CryptocoinAPI api = null;
    public static String owner = "";
    public static String currencyName = "";
    public static double tax = 0.0;
    public static boolean buyerorseller = false;
    public static boolean salesTax = false;
    public static double mult = 0;
    public static int confirms = 2;
    public static double minWithdraw = 0;
    // public static BitcoinAPI bapi = null;
    public static Logger log = null;
    public static SatoshisEconAPI econ = null;
    public static VaultEconAPI vecon = null;
    public static DatabaseScanner scanner = null;
    public static NetworkParameters network = null;
    private SystemCheckThread syscheck = null;
    public static FileConfiguration config= null;

    @Override
    public void onDisable() {
        api.saveWallet();
        Util.serializeChecking(CoinListener.pending);
    }

    @Override
    public void onEnable() {
        log = getLogger();
        setupDatabase();
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
        owner = config.getString("satoshis.owner");
        currencyName = config.getString("satoshis.currency-name");
        tax = config.getDouble("satoshis.tax");

        buyerorseller = config.getBoolean("satoshis.is-buyer-responsible");
        salesTax = config.getBoolean("satoshis.sales-tax");
        minWithdraw = config.getDouble("satoshis.min-withdraw");
        mult = config.getDouble("satoshis.multiplier");

        syscheck = new SystemCheckThread(config.getInt("self-check.delay"), config.getBoolean("self-check.startup"));
        econ = new SatoshisEconAPI();
        econ.buyerorseller = buyerorseller;
        api = new BitcoinAPI();
        api.loadEcon(config.getBoolean("bitcoin.testnet") ? NetworkType.TEST : NetworkType.PRODUCTION, config.getInt("satoshis.confirms"));
        scanner = new DatabaseScanner(this);
        syscheck.start();
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("deposit").setExecutor(new DepositCommand());
        this.getCommand("withdraw").setExecutor(new WithdrawCommand());
        this.getCommand("money").setExecutor(new MoneyCommand());
        this.getCommand("syscheck").setExecutor(new CheckCommand());
        this.getCommand("transact").setExecutor(new SendCommand());
        this.getCommand("credit").setExecutor(new CreditCommand());
        this.getCommand("debit").setExecutor(new DebitCommand());
        this.getCommand("satoshis").setExecutor(new AdminCommand());
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
            log.info("Metrics started!");
        } catch (IOException e) {
            log.info("Metrics disabled.");
        }

        if (config.getBoolean("satoshis.use-vault")) {
            activateVault();
        }
        log.info("Satoshis loaded sucessfully!");
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
        Util.saveAccount(Util.loadAccount(event.getPlayer().getName()));
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(AccountEntry.class);
        return list;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {
        return true;
    }

    private void setupDatabase() {
        try {
            getDatabase().find(AccountEntry.class).findRowCount();
        } catch (PersistenceException ex) {
            log.log(Level.INFO, "Installing database for {0} due to first time usage", getDescription().getName());
            installDDL();
        }
    }

    public AccountEntry getAccount(String name) {
        return getDatabase().find(AccountEntry.class).where().ieq("playerName", name).findUnique();
    }

    public void saveAccount(AccountEntry ae) {
        getDatabase().save(ae);
    }

    private boolean activateVault() {
        log.info("Attempting to activate Satoshis Vault support...");
        Plugin vault = Bukkit.getServer().getPluginManager().getPlugin("Vault");
        if (vault == null) {
            log.warning("Vault support disabled.");
            return false;
        }
        vecon = new VaultEconAPI(this);
        getServer().getServicesManager().register(Economy.class, vecon, this, ServicePriority.Highest);
        log.warning("Vault support enabled.");
        return true;
    }

    @EventHandler
    public void playerLogin(PlayerLoginEvent e) {
        saveAccount(Util.loadAccount(e.getPlayer().getName()));
    }

    public void readdTransactions() {
        List<Transaction> toAdd = Util.loadChecking();
        for (Transaction tx : toAdd) {
            Futures.addCallback(tx.getConfidence().getDepthFuture(Satoshis.confirms), new TransactionListener()); // TODO: I don't even know what in the hell
        }
    }
}
