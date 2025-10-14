package com.pedrorok.ami.pathfinding.octree;

/**
 * Estados possíveis de um nó da octree.
 */
public enum NodeState {
    EMPTY,      // Região completamente livre (ar)
    SOLID,      // Região completamente ocupada (blocos sólidos)
    MIXED,      // Região com blocos mistos (pode ser subdividida)
    UNKNOWN     // Estado não determinado ainda
}
