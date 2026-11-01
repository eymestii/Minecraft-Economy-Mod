## Etapa 1: Base de la economía (sin ítems nuevos)

- Saldo por jugador guardado:

SavedData del mundo (recomendado): un mapa UUID → saldo guardado en el mundo. Permite consultar y modificar saldos de jugadores desconectados (por ejemplo /pay a alguien offline, o un top de ricos), y es más fácil de auditar.

Seguridad

Todo cambio de saldo pasa por una única clase, por ejemplo EconomyManager, con métodos como getBalance, deposit, withdraw y transfer.
transfer debe ser atómico: valida que el monto sea positivo, que haya fondos y que no sea a uno mismo, y solo entonces mueve el dinero.
Se guarda el saldo como long (no double), en la unidad más pequeña, para evitar errores de redondeo y desbordamientos.

- Comandos

3. Comandos

/balance y /balance <jugador>: ver saldo.
/pay <jugador> <monto>: para todos los jugadores.
/eco give|take|set <jugador> <monto>: solo para operadores (nivel de permiso 2).

- Moneda y valores iniciales configurables en Config.java.

Configuración (Config.java)
Saldo inicial, nombre y símbolo de la moneda, monto máximo por transacción. (enteros simples)

Estructura de código sugerida

com.mestisexo.economymod
├── EconomyMod.java          (clase principal, @Mod)
├── Config.java
├── economy/
│   ├── EconomyData.java     (SavedData: mapa UUID → saldo)
│   └── EconomyManager.java  (lógica: deposit, withdraw, transfer)
└── command/
    └── EconomyCommands.java (registro de comandos)

## Etapa 2: Ganar dinero (la parte divertida)

- Caja de envío (shipping bin), un bloque donde metes ítems y al amanecer se venden y te pagan, como en Stardew Valley.
- Tabla de precios por ítem, definida con datapacks o JSON para que sea fácil de ajustar.
- Mensaje en pantalla con el resumen de lo vendido.

## Etapa 3: Monedas físicas y billetera

- Ítems de moneda (cobre, plata, oro) que se pueden convertir a saldo y viceversa.
- Un ítem billetera que muestra tu saldo.

Así el dinero también se puede dar, intercambiar o perder.

## Etapa 4: Tiendas

Bloque tienda de jugador: pones un cofre con ítems y fijas un precio, y los demás compran con clic.
Tienda de aldeanos o NPC con stock rotativo.
Interfaz (GUI) para comprar y vender.

## Etapa 5: Economía dinámica

Oferta y demanda: si vendes mucho trigo, su precio baja y se recupera con los días. Es lo que hace que la economía se sienta viva.
Eventos aleatorios, como una “feria” que sube los precios de ciertos ítems.

## Etapa 6: Extras

Banco con intereses, trabajos (ganar dinero por minar, cultivar o pescar), subastas, tabla de clasificación de los más ricos y HUD con el saldo.
