package mochineko.java_discord_bot.listener;

import mochineko.java_discord_bot.manager.music.MusicManager;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.stream.Collectors;

public class ButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        for (int n = 1; n <= 5; n++) {
            if (event.getComponentId().equals("button_" + n)) {
                MessageChannelUnion channel = event.getChannel();
                List<MessageTopLevelComponentUnion> components = event.getInteraction().getMessage().getComponents();
                List<Button> button_list = components.stream()
                        .filter(component -> component instanceof ActionRow)
                        .map(component -> (ActionRow) component)
                        .flatMap(row -> row.getButtons().stream())
                        .toList();


                List<Button> disable_button_list = button_list.stream()
                        .map(Button::asDisabled)
                        .collect(Collectors.toList());

                List<ActionRow> newRows = List.of(ActionRow.of(disable_button_list));
                event.editComponents(newRows).queue();

                MusicManager.getInstance().selectPlay(channel.asTextChannel(), n - 1);
            }
        }
    }

}
