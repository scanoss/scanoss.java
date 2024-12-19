// SPDX-License-Identifier: MIT
/*
 * Copyright (c) 2024, SCANOSS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.scanoss.settings;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents the Bill of Materials (BOM) rules configuration for the SCANOSS scanner.
 * This class defines rules for modifying the scan results through include,
 * ignore, remove, and replace operations.
 */
@Data
@Builder
public class Bom {

    /**
     * List of include rules for adding context when scanning.
     * These rules are sent to the SCANOSS API and have a higher chance of being
     * considered part of the resulting scan.
     */
    private final @Singular("include") List<Rule> include;

    /**
     * List of ignore rules for excluding certain components .
     * These rules are sent to the SCANOSS API.
     */
    private final @Singular("ignore") List<Rule> ignore;

    /**
     * List of remove rules for excluding components from results after scanning.
     * These rules are applied to the results file after scanning and are processed
     * on the client side.
     */
    private final @Singular("remove") List<RemoveRule> remove;

    /**
     * List of replace rules for substituting components after scanning.
     * These rules are applied to the results file after scanning and are processed
     * on the client side. Each rule can specify a new PURL and license for the
     * replacement component.
     */
    private final @Singular("replace") List<ReplaceRule> replace;


    /**
     * Cached list of replace rules sorted by priority.
     * This list is lazily initialized when first accessed through
     * getReplaceRulesByPriority().
     *
     * @see #getReplaceRulesByPriority()
     */
    private final List<ReplaceRule> sortedReplace;


    /**
     * Sorts replace rules by priority from highest to lowest:
     * 1. Rules with both purl/path (most specific)
     * 3. Rules with only purl
     * 4. Rules with only path (least specific)
     *
     * @return A new list containing the sorted replace rules
     */
    public List<ReplaceRule> getReplaceRulesByPriority() {
        if (sortedReplace == null) {
            List<ReplaceRule> sortedReplace = new ArrayList<>(replace);
            sortedReplace.sort(new RuleComparator());
            return sortedReplace;
        }
        return sortedReplace;
    }

}

