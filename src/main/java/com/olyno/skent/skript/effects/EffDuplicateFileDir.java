package com.olyno.skent.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.olyno.skent.skript.events.bukkit.ChangeEvent;
import com.olyno.skent.skript.events.bukkit.CopyEvent;
import com.olyno.skent.util.skript.AsyncEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Name("Duplicate File or directory.")
@Description("Duplicate the contents of a file or a directory without its parent path.")
@Since("BCD-Fork")
public class EffDuplicateFileDir extends AsyncEffect {

	static{
		registerAsyncEffect(EffDuplicateFileDir.class, "duplicate %paths% to %path%");
	}

	private Expression<Path> sources;
	private Expression<Path> target;

	@Override
	protected boolean initAsync(Expression<?>[] expr, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		this.sources = (Expression<Path>) expr[0];
		this.target = (Expression<Path>) expr[1];
		return true;
	}

	@Override
	protected void execute(Event e) {
		Path[] sourceFiles = sources.getArray(e);
		Path targetFile = target.getSingle(e);
		try {
			for (Path sourceFile : sourceFiles) {
				if (Files.exists(sourceFile)) {
					Path trimPath = sourceFile.getParent().relativize(sourceFile); //Remove parent path from source file.
					Path duplicated = targetFile.resolve(trimPath); //Append parent location of target to the source file, creating an accurate "duplicate".
					if(!targetFile.toFile().exists()){
						targetFile.toFile().createNewFile();
					}
					Files.copy(sourceFile, duplicated, StandardCopyOption.REPLACE_EXISTING);
					if (Files.isDirectory(sourceFile)) {
						copyDir(sourceFile, duplicated);
					}
					new ChangeEvent(targetFile);
					new CopyEvent(sourceFile, targetFile);
				} else {
					throw new FileNotFoundException();
				}
			}
		} catch (IOException ex) {
			Skript.exception(ex);
		}
	}

	// Source: https://stackoverflow.com/a/60621544/8845770
	// Pattern: "duplicate %sources% to %targets%"
	private void copyDir(Path source, Path target) throws IOException {
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectories(target.resolve(source.relativize(dir)));
				return FileVisitResult.CONTINUE;
			}


		});
}
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "duplicate %" + sources.toString(e, debug) + " to " + target.toString(e, debug);
	}
}
