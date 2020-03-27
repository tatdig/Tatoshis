Tatoshis
=

Current version: 1.3.1

Minecraft with real life cryptocurrency! Minecraft 1.15+ Vault Economy Provider.
=
TESTNET server with ChestShop3 and Tatoshis running on mc.itservicefl.com. Try it!
=

Ah, economy plugins... Without them, we would have no in game currency! But what if this in game currency... was also out-of-game currency?  
Have fun and develop some business skills!

*Enter Tatoshis!*
Coupled with the TDCoin P2P digital currency, you can trade in game with real crypto coins!

The design is simple:  
1) Player starts with no money  
2) Player can add money to their account by sending TDCoin to a certain address.  
3) Player can withdraw money from their account to any TDCoin address.  
4) Trade takes place as usual, as the plugin hooks to "Vault". Theoretically any shop/purchase plugin supporting "Vault" should work.  
5) Tax system allows for a "Sales Tax" on each money transfer. The settings for whether the buyer or the seller is held responsible for tax, and for tax rates, are available in the config.  

Commands:  
=
/money - List current amount of TDCcoin in your minecraft account.  
/transact <player> <amount> - Transfer's money from your account to the selected player's account.  
/deposit - Get a TDCoin address to send a deposit to. The next transaction to that address will fund your account.  
/withdraw \<address\> [amount] - Transfers money from your account to your TDCoin wallet. Must have at least the amount specified in the config. If the amount is left off, it will transfer all of your funds.  
/tatoshis info - Print basic debugging and economy info.  
/tatoshis reset - Delete and re-download the block chain.  
/syscheck - Verify that the current TDCcoin holdings tally with the balances of all in-game accounts.  
/credit <player> <amount> - Add the specified amount to the given player's balance  
/debit <player> <amount> - Subtract the specified amount from the given player's balance  
/reward <player> <amount> - Transfer's money from server account to the selected player's account. Can be used in console and command block.
  This command understand @ selectors.

Permissions:
=
tatoshis.* - All commands  
tatoshis.money - /money  
tatoshis.transact - /transact  
tatoshis.withdraw - /withdraw

tatoshis.admin - /admin  
tatoshis.info - /syscheck  
tatoshis.credit - /credit  
tatoshis.debit - /debit

Where to get coins?
=
Please feel free to request TESTNET TDCoins. 10 TDCTN will be given for testing purpose.

Mine coins with TDCoin android miner - https://www.tdcoincore.org/download/android/

Purchase mining contract - https://www.tdcoincore.org/mining/

