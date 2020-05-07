/**
 * Copyright (c) 2011, Regents of the University of California
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * <p/>
 * 3. Neither the name of the University of California, Berkeley nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.berkeley.cs.wise.benchmarks;

import edu.berkeley.cs.wise.benchmarks.rbtree.RedBlackTree;
import edu.berkeley.cs.wise.benchmarks.rbtree.RedBlackTreeNode;
import edu.utexas.stac.Cost;

import java.util.Scanner;

/**
 * @author Koushik Sen <ksen@cs.berkeley.edu>
 * @author Jacob Burnim <jburnim@cs.berkeley.edu>
 */
public class RedBlackTreeSearch {

    public static RedBlackTree build(Integer ints[]) {
        RedBlackTree rbTree = new RedBlackTree();
        for (Integer i: ints)
            rbTree.treeInsert(new RedBlackTreeNode(i));
        return rbTree;
    }

    public static RedBlackTreeNode search(RedBlackTree rbTree, Integer i) {
        return rbTree.treeSearch(rbTree.root(), i);
    }

    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);

        RedBlackTree tree = new RedBlackTree();
        Scanner in = new Scanner(System.in);

        for (int i = 0; i < N; i++) {
            int data = in.nextInt();
            tree.treeInsert(new RedBlackTreeNode(data));
        }

        Cost.reset();

        int data = in.nextInt();
        tree.treeSearch(tree.root(), data);
    }
}
