package c32.compiler.except;

import c32.compiler.Location;
import lombok.Getter;

@Getter
public class CompilerException extends RuntimeException {
	private final Location location;
	private final String rawMessage;


	public CompilerException(Location location, String message) {
		super(message);
		this.location = location;
		this.rawMessage = message;
	}

	public CompilerException(Location location, String message, Throwable cause) {
		super(message, cause);
		this.location = location;
		this.rawMessage = message;
	}


	public CompilerException(Location location, String message, String rawMessage) {
		super(message);
		this.location = location;
		this.rawMessage = rawMessage;
	}

	public CompilerException(Location location, String message, Throwable cause, String rawMessage) {
		super(message, cause);
		this.location = location;
		this.rawMessage = rawMessage;
	}

	public CompilerException(Location location, Throwable cause, String rawMessage) {
		super(cause);
		this.location = location;
		this.rawMessage = rawMessage;
	}

}
