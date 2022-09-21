## Problem

The results of the `BigDecimal` constructor can be somewhat unpredictable. One
might assume that writing `new BigDecimal(0.1)` in Java creates a `BigDecimal`
which is exactly equal to `0.1` (an unscaled value of `1`, with a scale of
`1`), but it is actually equal to
`0.1000000000000000055511151231257827021181583404541015625`. 

This is because
`0.1` cannot be represented exactly as a `double` (or, for that matter, as a
binary fraction of any finite length). Thus, the value that is being passed in
to the constructor is not exactly equal to `0.1`, appearances notwithstanding.
