package AnalizadorSintactico;

/**
 * Enumerado con todas las instrucciones del codigo P
 */
public enum InstruccionesPila {
	apila{
        @Override
        public String toString() {
            return "apila";
        }
    },
	apila_dir{
        @Override
        public String toString() {
            return "apila-dir";
        }
    },
	desapila_dir{
        @Override
        public String toString() {
            return "desapila-dir";
        }
    },
	menor{
        @Override
        public String toString() {
            return "menor";
        }
    },	        
	mayor{
        @Override
        public String toString() {
            return "mayor";
        }
    },	  
	menoroigual{
        @Override
        public String toString() {
            return "menoroigual";
        }
    },	  
	mayoroigual{
        @Override
        public String toString() {
            return "mayoroigual";
        }
    },	  
	igual{
        @Override
        public String toString() {
            return "igual";
        }
    },	  
	distinto{
        @Override
        public String toString() {
            return "distinto";
        }
    },	  
	suma{
        @Override
        public String toString() {
            return "suma";
        }
    },	  
	resta{
        @Override
        public String toString() {
            return "resta";
        }
    },	  
	or_logica{
        @Override
        public String toString() {
            return "or-logica";
        }
    },	  
	multiplica{
        @Override
        public String toString() {
            return "multiplica";
        }
    },	  
	divide{
        @Override
        public String toString() {
            return "divide";
        }
    },	  
	modulo{
        @Override
        public String toString() {
            return "modulo";
        }
    },	  
	and_logica{
        @Override
        public String toString() {
            return "and-logica";
        }
    },	  
	desp_izq{
        @Override
        public String toString() {
            return "desp_izq";
        }
    },	  
	desp_der{
        @Override
        public String toString() {
            return "desp_der";
        }
    },	  
	niega{
        @Override
        public String toString() {
            return "niega";
        }
    },	  
	menos{
        @Override
        public String toString() {
            return "menos";
        }
    },	  
	casting_integer{
        @Override
        public String toString() {
            return "casting(integer)";
        }
    },	  		
	casting_boolean{
        @Override
        public String toString() {
            return "casting(boolean)";
        }
    },	  
	casting_natural{
        @Override
        public String toString() {
            return "casting(natural)";
        }
    },	  
	casting_float{
        @Override
        public String toString() {
            return "casting(float)";
        }
    },	  
	casting_character{
        @Override
        public String toString() {
            return "casting(character)";
        }
    },
    in{
        @Override
        public String toString() {
            return "in";
        }
    },
    out{
        @Override
        public String toString() {
            return "out";
        }
    },
    swap1{
        @Override
        public String toString() {
            return "swap1";
        }
    },
    swap2{
        @Override
        public String toString() {
            return "swap2";
        }
    },
    stop{
        @Override
        public String toString() {
            return "stop";
        }
    },
    ir_a{
        @Override
        public String toString() {
            return "ir-a";
        }
    },
    ir_f{
        @Override
        public String toString() {
            return "ir-f";
        }
    },
    etiqueta{
        @Override
        public String toString() {
            return "etiqueta";
        }       
    },
    apila_ind{
    	@Override
        public String toString() {
            return "apila-ind";
        }       
    },
    desapila_ind{
    	@Override
        public String toString() {
            return "desapila-ind";
        }     
    },
    copia{
    	@Override
        public String toString() {
            return "copia";
        }     
    },
    ir_ind{
    	@Override
        public String toString() {
            return "ir-ind";
        }     
    },
    mueve{
    	@Override
        public String toString() {
            return "mueve";
        }     
    },
    desapila{
    	@Override
        public String toString() {
            return "desapila";
        }     
    },
    ir_v{
    	@Override
        public String toString() {
            return "ir-v";
        }     
    },
}	
