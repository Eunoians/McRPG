package us.eunoians.mcrpg.gui;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;

import java.util.ArrayList;
import java.util.List;

public class RedeemStoredGUI extends GUI {

  private static FileManager fm = McRPG.getInstance().getFileManager();

  private static FileManager.Files file = FileManager.Files.REDEEM_GUI;

  public RedeemStoredGUI(McRPGPlayer player){
    super(new GUIBuilder("RedeemGUI", fm.getFile(file), player));
    for(int i = 0; i < this.getGui().getInv().getSize(); i ++){
      ItemStack item = this.getGui().getInv().getItem(i);
      if(item.hasItemMeta()){
        ItemMeta meta = item.getItemMeta();
        if((meta).hasLore()){
          List<String> newLore = new ArrayList<>(meta.getLore().size());
          for(String s : meta.getLore()){
            newLore.add(s.replace("%ExpAmount%", Integer.toString(player.getRedeemableExp())).replace("%LevelAmount%", Integer.toString(player.getRedeemableLevels())));
          }
          meta.setLore(newLore);
          item.setItemMeta(meta);
        }
      }
    }
  }
}
