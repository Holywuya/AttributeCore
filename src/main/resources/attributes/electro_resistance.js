// 雷元素抗性
// 减少受到的雷元素伤害

var priority = 43;
var combatPower = 1.0;
var attributeName = "electro_resistance";
var attributeType = "Other";
var placeholder = "electro_resistance";
var pattern = "雷元素抗性";
var patternSuffix = "";

// 注意: 抗性属性不需要实现 runDefense 函数
// 抗性会在 DamageBucket.applyResistances() 中自动应用
// 公式: damage * (1 - resistance / (resistance + 100))
// 例如: 50点雷抗性 -> 减伤 50/(50+100) = 33.3%
