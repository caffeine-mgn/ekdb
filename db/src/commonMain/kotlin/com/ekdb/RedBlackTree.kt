package com.ekdb

private const val RED = 0
private const val BLACK = 1


abstract class RedBlackTree<K : Any?, V> : Comparator<K> {

    protected val nil = Node(null as K, null as V)
    protected var root: Node = nil

    companion object {
        const val LEFT = -1
        const val RIGHT = 1
        const val BOTH = 2
        const val STOP = 0
    }

    inner class Node(val key: K, val value: V) {
        var color = BLACK
        var left: Node = nil
        var right: Node = nil
        var parent: Node = nil

        operator fun compareTo(other: Node): Int =
            compareTo(other.key)

        operator fun compareTo(other: K): Int =
            this@RedBlackTree.compare(key, other)
    }

    fun findNode(comparator: (Node) -> Int) = findNode(root, comparator)

    protected fun findNode(node: Node, comparator: (Node) -> Int) {
        if (root == nil) {
            return
        }
        val comparable = comparator(node)
        when {
            comparable == LEFT -> if (node.left != nil) findNode(node.left, comparator)
            comparable == RIGHT -> if (node.right != nil) findNode(node.right, comparator)
            comparable == STOP -> return
            comparable == BOTH -> {
                if (node.right != nil) {
                    findNode(node.right, comparator)
                }
                if (node.left != nil) {
                    findNode(node.left, comparator)
                }
            }
            else -> TODO()
        }
    }

    fun findNode(node: K): V? = findNode(root, node)

    protected fun findNode(root: Node, node: K): V? {
        if (root == nil) {
            return null
        }
        val comparable = compare(root.key, node)
        return when {
            comparable < 0 -> if (root.left != nil) findNode(root.left, node) else null
            comparable > 0 -> if (root.right != nil) findNode(root.right, node) else null
            comparable == 0 -> root.value
            else -> TODO()
        }
    }

    private fun findNode(findNode: Node, node: Node): Node? {
        if (root == nil) {
            return null
        }

        return when {
            findNode < node && node.left != nil -> findNode(findNode, node.left)
            findNode > node && node.right != nil -> findNode(findNode, node.right)
            findNode == node -> node
            else -> null
        }
    }

    fun put(key: K, value: V) {
        insert(Node(key, value))
    }

    private fun insert(node: Node) {
        var temp = root
        if (root == nil) {
            root = node
            node.color = BLACK
            node.parent = nil
        } else {
            node.color = RED
            while (true) {
                if (node < temp) {
                    if (temp.left == nil) {
                        temp.left = node
                        node.parent = temp
                        break
                    } else {
                        temp = temp.left
                    }
                } else if (node >= temp) {
                    if (temp.right == nil) {
                        temp.right = node
                        node.parent = temp
                        break
                    } else {
                        temp = temp.right
                    }
                }
            }
            fixTree(node)
        }
    }

    //Takes as argument the newly inserted node
    private fun fixTree(node: Node) {
        var node = node
        while (node.parent.color == RED) {
            var uncle = nil
            if (node.parent == node.parent.parent.left) {
                uncle = node.parent.parent.right

                if (uncle != nil && uncle.color == RED) {
                    node.parent.color = BLACK
                    uncle.color = BLACK
                    node.parent.parent.color = RED
                    node = node.parent.parent
                    continue
                }
                if (node == node.parent.right) {
                    //Double rotation needed
                    node = node.parent
                    rotateLeft(node)
                }
                node.parent.color = BLACK
                node.parent.parent.color = RED
                //if the "else if" code hasn't executed, this
                //is a case where we only need a single rotation
                rotateRight(node.parent.parent)
            } else {
                uncle = node.parent.parent.left
                if (uncle != nil && uncle.color == RED) {
                    node.parent.color = BLACK
                    uncle.color = BLACK
                    node.parent.parent.color = RED
                    node = node.parent.parent
                    continue
                }
                if (node == node.parent.left) {
                    //Double rotation needed
                    node = node.parent
                    rotateRight(node)
                }
                node.parent.color = BLACK
                node.parent.parent.color = RED
                //if the "else if" code hasn't executed, this
                //is a case where we only need a single rotation
                rotateLeft(node.parent.parent)
            }
        }
        root.color = BLACK
    }

    fun rotateLeft(node: Node) {
        if (node.parent != nil) {
            if (node == node.parent.left) {
                node.parent.left = node.right
            } else {
                node.parent.right = node.right
            }
            node.right.parent = node.parent
            node.parent = node.right
            if (node.right.left != nil) {
                node.right.left.parent = node
            }
            node.right = node.right.left
            node.parent.left = node
        } else {//Need to rotate root
            val right = root.right
            root.right = right.left
            right.left.parent = root
            root.parent = right
            right.left = root
            right.parent = nil
            root = right
        }
    }

    fun rotateRight(node: Node) {
        if (node.parent != nil) {
            if (node == node.parent.left) {
                node.parent.left = node.left
            } else {
                node.parent.right = node.left
            }

            node.left.parent = node.parent
            node.parent = node.left
            if (node.left.right != nil) {
                node.left.right.parent = node
            }
            node.left = node.left.right
            node.parent.right = node
        } else {//Need to rotate root
            val left = root.left
            root.left = root.left.right
            left.right.parent = root
            root.parent = left
            left.right = root
            left.parent = nil
            root = left
        }
    }

    //Deletes whole tree
    fun deleteTree() {
        root = nil
    }

    //Deletion Code .

    //This operation doesn't care about the new Node's connections
    //with previous node's left and right. The caller has to take care
    //of that.
    fun transplant(target: Node, with: Node) {
        if (target.parent == nil) {
            root = with
        } else if (target == target.parent.left) {
            target.parent.left = with
        } else
            target.parent.right = with
        with.parent = target.parent
    }

    fun delete(z: Node): Boolean {
        val z = findNode(z, root)
        if (z == null) return false
        val x: Node
        var y = z; // temporary reference y
        var y_original_color = y.color

        if (z.left == nil) {
            x = z.right
            transplant(z, z.right)
        } else if (z.right == nil) {
            x = z.left
            transplant(z, z.left)
        } else {
            y = treeMinimum(z.right)
            y_original_color = y.color
            x = y.right
            if (y.parent == z) {
                x.parent = y
            } else {
                transplant(y, y.right)
                y.right = z.right
                y.right.parent = y
            }
            transplant(z, y)
            y.left = z.left
            y.left.parent = y
            y.color = z.color
        }
        if (y_original_color == BLACK)
            deleteFixup(x)
        return true
    }

    fun deleteFixup(x: Node) {
        var x = x
        while (x != root && x.color == BLACK) {
            if (x == x.parent.left) {
                var w = x.parent.right
                if (w.color == RED) {
                    w.color = BLACK
                    x.parent.color = RED
                    rotateLeft(x.parent)
                    w = x.parent.right
                }
                if (w.left.color == BLACK && w.right.color == BLACK) {
                    w.color = RED
                    x = x.parent
                    continue
                } else if (w.right.color == BLACK) {
                    w.left.color = BLACK
                    w.color = RED
                    rotateRight(w)
                    w = x.parent.right
                }
                if (w.right.color == RED) {
                    w.color = x.parent.color
                    x.parent.color = BLACK
                    w.right.color = BLACK
                    rotateLeft(x.parent)
                    x = root
                }
            } else {
                var w = x.parent.left
                if (w.color == RED) {
                    w.color = BLACK
                    x.parent.color = RED
                    rotateRight(x.parent)
                    w = x.parent.left
                }
                if (w.right.color == BLACK && w.left.color == BLACK) {
                    w.color = RED
                    x = x.parent
                    continue
                } else if (w.left.color == BLACK) {
                    w.right.color = BLACK
                    w.color = RED
                    rotateLeft(w)
                    w = x.parent.left
                }
                if (w.left.color == RED) {
                    w.color = x.parent.color
                    x.parent.color = BLACK
                    w.left.color = BLACK
                    rotateRight(x.parent)
                    x = root
                }
            }
        }
        x.color = BLACK
    }

    fun treeMinimum(subTreeRoot: Node): Node {
        var subTreeRoot = subTreeRoot
        while (subTreeRoot.left != nil) {
            subTreeRoot = subTreeRoot.left
        }
        return subTreeRoot
    }
}