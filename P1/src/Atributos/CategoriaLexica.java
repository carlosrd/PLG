package Atributos;

/**
 * Enumerado que contiene todas las categorias lexicas reconocidas
 * 
 */
public enum CategoriaLexica {

	LitNatural,
	LitEntero,
	LitDecimal,
	LitCaracter,
	LitBooleano,
	CastingNatural{
        @Override
        public String toString() {
            return "casting(natural)";
        }
    },
	CastingEntero{
        @Override
        public String toString() {
            return "casting(integer)";
        }
    },
	CastingDecimal{
        @Override
        public String toString() {
            return "casting(float)";
        }
    },
	CastingCaracter{
        @Override
        public String toString() {
            return "casting(character)";
        }
    },
    TArray,
    TTupla,
    TProc,		// No se usara, pero por regularidad en la herencia de ExpTipo
	PalabraReservada,
	TError,
	TFin,
	Identificador,	
	OpAsignConst{
        @Override
        public String toString() {
            return ":=";
        }
    }, 
	OpAsignVar{
        @Override
        public String toString() {
            return "=";
        }
    }, 
	ComillaSimple{

        @Override
        public String toString() {
            return "'";
        }
    }, 
	Coma{

        @Override
        public String toString() {
            return ",";
        }
    }, 
    PuntoComa{

        @Override
        public String toString() {
            return ";";
        }
    },
    AbreParentesis{

        @Override
        public String toString() {
            return "(";
        }
    },
    CierraParentesis{

        @Override
        public String toString() {
            return ")";
        }
    },
    AbreLlave{

        @Override
        public String toString() {
            return "{";
        }
    },
    CierraLlave{

        @Override
        public String toString() {
            return "}";
        }
    },
    AbreCorchete{

        @Override
        public String toString() {
            return "[";
        }
    },
    CierraCorchete{

        @Override
        public String toString() {
            return "]";
        }
    },
    Punto{

        @Override
        public String toString() {
            return ".";
        }
    },
    DosPuntos{

        @Override
        public String toString() {
            return ":";
        }
    },
    Igual{

        @Override
        public String toString() {
            return "==";
        }
    },
    Distinto{

        @Override
        public String toString() {
            return "!=";
        }
    },
    Mas{

        @Override
        public String toString() {
            return "+";
        }
    },
    Menos{

        @Override
        public String toString() {
            return "-";
        }
    },
    Por{

        @Override
        public String toString() {
            return "*";
        }
    },
    Divide{

        @Override
        public String toString() {
            return "/";
        }
    },
    Modulo{

        @Override
        public String toString() {
            return "%";
        }
    },
    DesplazIz{

        @Override
        public String toString() {
            return "<<";
        }
    },
    DesplazDer{

        @Override
        public String toString() {
            return ">>";
        }
    }, 
    MayorIgual{

        @Override
        public String toString() {
            return ">=";
        }
    },
    MenorIgual{

        @Override
        public String toString() {
            return "<=";
        }
    },
    Mayor{
	    @Override
	    public String toString() {
	        return ">";
	    }
	},
    Menor{
	    @Override
	    public String toString() {
	        return "<";
	    }
	}, 
	BarraBaja{
        @Override
        public String toString() {
            return "_";
        }
    },
 }