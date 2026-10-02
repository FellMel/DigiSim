package de.parresum.digisim.annotations;

import de.parresum.digisim.lib.CircuitPart;
import de.parresum.digisim.lib.wire.Wire;

public interface PartConnector {
   public void joinWire(Wire wire, CircuitPart part, String pinNumber);
}
