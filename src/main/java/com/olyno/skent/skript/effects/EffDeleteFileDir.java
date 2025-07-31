package com.olyno.skent.skript.effects;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.bukkit.event.Event;

import com.olyno.skent.skript.events.bukkit.ChangeEvent;
import com.olyno.skent.skript.events.bukkit.DeleteEvent;
import com.olyno.skent.util.skript.AsyncEffect;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Delete File or directory")
@Description("Deletes a file or a directory.")
@Examples({
    "command delete:\n" +
        "\ttrigger:\n" +
        "\t\tdelete file path \"plugins/Skript/scripts/myAwesomeScript.sk\"\n" +
        "\t\tbroadcast \"Oh no, my awesome script!\"",

    "# If you need to wait the end of the effect before execute a part of your code, you can\n" +
    "# use the keyword \"sync\" before.\n" +
    "# The code after this effect section will be executed when the effect section has finished to be executed.\n\n" +
    "command delete:\n" +
        "\ttrigger:\n" +
        "\t\tsync delete file path \"plugins/Skript/scripts/myAwesomeScript.sk\"\n" +
        "\t\tbroadcast \"Oh no, my awesome script!\""
})
@Since("1.0")

public class EffDeleteFileDir extends AsyncEffect {

    static {
        registerAsyncEffect(EffDeleteFileDir.class,
            "delete path %path%"
        );
    }

    private Expression<Path> paths;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean initAsync(Expression<?>[] expr, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        paths = (Expression<Path>) expr[0];
        return true;
    }

    @Override
    protected void execute(Event e) {
        Path[] pathList = paths.getArray(e);
        for (Path path : pathList) {
            if (Files.exists(path)) {
                try {
                    if (Files.isDirectory(path)) {
                        Files.walk(path)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete); // Can't use java nio here due to DirectoryNotEmptyException.
                    } else {
                        Files.delete(path);
                    }
                    new DeleteEvent(path);
                    new ChangeEvent(path.getParent());
                } catch (IOException ex) {
                    if (Files.exists(path)) {
                        Skript.exception(ex);
                    }
                }
            }
        }
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "delete " + paths.toString(e, debug);
    }

}
