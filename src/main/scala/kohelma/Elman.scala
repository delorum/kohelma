package kohelma

import java.io._


/**
 * Класс-контейнер для одного тренировочного набора
 * @param inputs - массив значений на входе. Длина массива должна соответствовать количеству нейронов во входном слое сети, на которой применяется набор!
 * @param expected - ожидаемый массив значений на выходе. Длина массива должна соответствовать количеству нейронов выходного слоя сети, на которой применяется набор!
 */
case class TrainingSet(inputs:Array[Double], expected:Array[Double])

object Elman {
  def apply(input_neurons:Int,
            hidden_neurons:Int,
            output_neurons:Int,
            wih:Array[Array[Double]],
            wch:Array[Array[Double]],
            who:Array[Array[Double]],
            whc:Array[Double],
            context:Array[Double]):Elman = {
    new Elman(input_neurons, hidden_neurons, output_neurons, wih, wch, who, whc, context)    
  }

  def apply(input_neurons:Int,
            hidden_neurons:Int,
            output_neurons:Int,
            file:String):Elman = {
    new Elman(input_neurons, hidden_neurons, output_neurons, file)
  }
  
  def apply(file:String):Elman = {
    val (input_neurons, hidden_neurons, output_neurons, wih, wch, who, whc, context) = loadParams(file)
    new Elman(input_neurons, hidden_neurons, output_neurons, wih, wch, who, whc, context)
  }

  def loadParams(file:String):(Int, Int, Int, Array[Array[Double]], Array[Array[Double]], Array[Array[Double]], Array[Double], Array[Double]) = {
    val reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))
    var input_neurons, hidden_neurons, output_neurons = 0
    var wih:Array[Array[Double]] = null 
    var wch:Array[Array[Double]] = null
    var who:Array[Array[Double]] = null
    var whc:Array[Double] = null
    var context:Array[Double] = null
    var i, j = 0
    var current_loading_array = ""
    while(reader.ready) {
      val line = reader.readLine
      line match {
        case "input_neurons" => current_loading_array = "input_neurons"
        case "hidden_neurons" => current_loading_array = "hidden_neurons"
        case "output_neurons" => current_loading_array = "output_neurons"
        case "wih" => current_loading_array = "wih"
        case "wch" => current_loading_array = "wch"
        case "who" => current_loading_array = "who"
        case "whc" => current_loading_array = "whc"
        case "context" => current_loading_array = "context"
        case "---" =>
          i = 0; j = 0
        case elem =>
          current_loading_array match {
            case "input_neurons" => input_neurons = elem.toInt; _createArrays()
            case "hidden_neurons" => hidden_neurons = elem.toInt; _createArrays()
            case "output_neurons" => output_neurons = elem.toInt; _createArrays()
            case "wih" => _insertElemTo2DimArray(wih, elem.toDouble)
            case "who" => _insertElemTo2DimArray(who, elem.toDouble)
            case "whc" =>
              whc(i) = elem.toDouble
              i += 1
            case "context" =>
              context(i) = elem.toDouble
              i += 1
            case _ => // do nothing
          }
      }
    }

    def _createArrays() {
      if(input_neurons != 0 && hidden_neurons != 0 && output_neurons != 0) {
        if(wih == null) wih = Array.ofDim[Double](input_neurons+1,   hidden_neurons)
        if(wch == null) wch = Array.ofDim[Double](hidden_neurons+1, hidden_neurons)
        if(who == null) who = Array.ofDim[Double](hidden_neurons+1,  output_neurons)
        if(whc == null) whc = Array.ofDim[Double](hidden_neurons)
        if(context == null) context = Array.ofDim[Double](hidden_neurons)
      }
    }

    def _insertElemTo2DimArray(arr:Array[Array[Double]], elem:Double) {
      arr(i)(j) = elem
      j += 1
      if(j >= arr(0).length) {
        i += 1
        j = 0
      }
    }

    (input_neurons, hidden_neurons, output_neurons, wih, wch, who, whc, context)
  }
}
/**
 * Класс моделирует рекуррентную нейронную сеть Элмана с одним скрытым и одним контекстным слоями. Можно загрузить веса сразу,
 * в качестве параметров, либо сформировать новые в процессе обучения.
 *
 * @param input_neurons - количество нейронов во входном слое
 * @param hidden_neurons - количество нейронов в скрытом слое
 * @param output_neurons - количество нейронов в выходном слое
 * @param wih - связи от входного слоя к скрытому (от каждого ко всем) плюс дополнительные "смещения" (массив wih(input_neurons))
 * @param wch - связи от контекстного слоя к скрытому (от каждого ко всем) плюс дополнительные "смещения" (массив wсh(context_neurons))
 * @param who - связи от скрытого слоя к выходному (от каждого ко всем) плюс дополнительные "смещения" (массив who(hidden_neurons))
 * @param whc - связи от скрытого слоя к контекстному (один к одному)
 * @param context - контекстный слой нейронов
 */
  class Elman(val input_neurons:Int,
              val hidden_neurons:Int,
              val output_neurons:Int,
              private var wih:Array[Array[Double]],
              private var wch:Array[Array[Double]],
              private var who:Array[Array[Double]],
              private var whc:Array[Double],
              private var context:Array[Double]) {
  def this(input_neurons:Int, hidden_neurons:Int, output_neurons:Int) {
    this(input_neurons, hidden_neurons, output_neurons,
         wih = Array.ofDim[Double](input_neurons+1,   hidden_neurons),
         wch = Array.ofDim[Double](hidden_neurons+1, hidden_neurons),
         who = Array.ofDim[Double](hidden_neurons+1,  output_neurons),
         whc = Array.ofDim[Double](hidden_neurons),
         context = Array.ofDim[Double](hidden_neurons))
    assignRandomWeights()
  }
  def this(input_neurons:Int, hidden_neurons:Int, output_neurons:Int, file:String) {
    this(input_neurons, hidden_neurons, output_neurons,
         wih = Array.ofDim[Double](input_neurons+1,   hidden_neurons),
         wch = Array.ofDim[Double](hidden_neurons+1, hidden_neurons),
         who = Array.ofDim[Double](hidden_neurons+1,  output_neurons),
         whc = Array.ofDim[Double](hidden_neurons),
         context = Array.ofDim[Double](hidden_neurons))
    load(file)
  }
  
  val context_neurons:Int = hidden_neurons  // количество нейронов в контекстном слое. Равняется количеству нейронов скрытого слоя

  require(wih.length == input_neurons+1)
  require(!wih.isEmpty && wih(0).length == hidden_neurons)
  require(wch.length == context_neurons+1)
  require(!wch.isEmpty && wch(0).length == hidden_neurons)
  require(who.length == hidden_neurons+1)
  require(!who.isEmpty && who(0).length == output_neurons)
  require(whc.length == context_neurons)
  require(context.length == context_neurons)

  private var hidden = Array.ofDim[Double](hidden_neurons)    // промежуточные значения на нейронах скрытого слоя

  /**
   * Метод инициализирует величины межнейронных связей случайными значениями от -0.5 до 0.5
   */
  private def assignRandomWeights() {
    for {
      i <- 0 to input_neurons
      j <- 0 until hidden_neurons
    } wih(i)(j) = math.random - 0.5 // случайные числа от -0.5 до 0.5

    for {
      i <- 0 to context_neurons
      j <- 0 until hidden_neurons
    } wch(i)(j) = math.random - 0.5

    for {
      i <- 0 to hidden_neurons
      j <- 0 until output_neurons
    } who(i)(j) = math.random - 0.5

    for {
      i <- 0 until context_neurons
    } whc(i) = 1  // для связей от скрытого слоя к контекстному выставляем коэффициент передачи = 1
  }

  /**
   * Пороговая функция на нейронах, используется для вычисления отклика на нейронах
   * @param value - входное значение
   * @return - значение функции от данного аргумента
   */
  def sigmoid(value:Double) = 1.0 / (1.0 + math.exp(-value))

  /**
   * Производная от пороговой функции, используется в алгоритме коррекции весов методом обратного распространения ошибки
   * @param value - входное значение
   * @return - значение функции от данного аргумента
   */
  def sigmoidDerivative(value:Double) = value * (1.0 - value)

  /**
   * Функция вычисляет отклик сети в ответ на данный набор входных значений
   * @param inputs - массив значений на нейронах входного слоя. Длина массива должна равняться количеству нейронов входного слоя!
   * @return - массив значений на выходе
   */
                    def outputs(inputs:Array[Double]) = {
                      // вычисляем значения на нейронах скрытого слоя
                      for(h <- 0 until hidden_neurons)
                        hidden(h) = sigmoid(
                          inputs.zipWithIndex.foldLeft(0.0)({
                            case (sum, (input_elem, i)) => sum + input_elem*wih(i)(h)
                          }) + wih(input_neurons)(h) +  // прибавляем дополнительное "смещение"
                            context.zipWithIndex.foldLeft(0.0)({
                              case (sum, (context_elem, c)) => sum + context_elem*wch(c)(h)
                            }) + wch(context_neurons)(h)  // прибавляем дополнительное "смещение"
                        )

                      // вычисляем значения на нейронах выходного слоя
                      val outputs = Array.ofDim[Double](output_neurons)
                      for(o <- 0 until output_neurons)
                        outputs(o) = sigmoid(
                          hidden.zipWithIndex.foldLeft(0.0)({
                            case (sum, (hidden_elem, h)) => sum + hidden_elem*who(h)(o)
                          }) + who(hidden_neurons)(o)   // прибавляем дополнительное "смещение"
                        )

                      // обновляем значения на нейронах контекстного слоя (присваиваем туда значения из скрытогоостана)
                      for(c <- 0 until context_neurons)
                        context(c) = hidden(c)*whc(c)

                      // возвращаем результат
                      outputs
                    }


   private var stop_trainig = false
   def stopTraining() {stop_trainig = true}

  /**
   * Функция осуществляет обучение данной нейронной сети Элмана
   * @param training_session - массив тренировочных наборов, которые будут использоваться в обучении. Длина массива должна быть больше 0!
   * @param training_reps - количество итераций обучения
   * @param error_threshold - порог для ошибки распознавания. Когда сеть достигнет такого значения для ошибки, обучение будет прервано
   * @param learn_rate - коэффициент обучения (величина коррекции весов за одну итерацию обучения)
   * @param reporter - функция для передачи различной отладочной информации из алгоритма обучения в процессе его работы
  */
  def train(training_session:Array[TrainingSet], training_reps:Int = 100000000, error_threshold:Double = 0.09, learn_rate:Double = 0.2, reporter:String => Unit = {message => println(message)}) {
    // в эти переменные будем сохранять лучшие веса и лучшую ошибку в процессе обучения
    var best_who = Array.ofDim[Double](hidden_neurons+1,  output_neurons)
    var best_wih = Array.ofDim[Double](input_neurons+1,   hidden_neurons)
    var worst_erro = 100.0                  // наилучшая ошибка за все обучение
    var current_worst_erro = 0.0            // наихудшая ошибка в данной тренировочной сессии
    //var sessions_without_correction = 0     // счетчик сессий, за которые наилучшая ошибка не обновлялась

    var iteration = 0     // общий счетчик шагов обучения
    var next_in_set = 0   // счетчик тренировочных наборов внутри сессии training_session
    var next_session = 0  // счетчик повторений тренировочной сессии training_session
    stop_trainig = false
    while(next_session < training_reps && worst_erro > error_threshold && !stop_trainig) {
      // получаем очередной тренировочный набор к нашей сети на входные значения из данного тренировочного набора
      val TrainingSet(inputs, expected) = training_session(next_in_set)
      val actual = outputs(inputs)
      
      // вычисляем массив ошибок на выходном слое сети
      val erro = Array.ofDim[Double](output_neurons)
      for(o <- 0 until output_neurons) {
        erro(o) = sigmoidDerivative(actual(o))*(expected(o) - actual(o))
      }
      
      // вычисляем массив ошибок на скрытом слое сети
      val errh = Array.ofDim[Double](hidden_neurons)
      for(h <- 0 until hidden_neurons)
        errh(h) = sigmoidDerivative(hidden(h))*erro.zipWithIndex.foldLeft(0.0)({
          case (sum, (erro_elem, o)) => sum + erro(o)*who(h)(o)
        })
      
      // обновляем веса от скрытого слоя к выходному
      for(o <- 0 until output_neurons) {        
        who(hidden_neurons)(o) += learn_rate*erro(o)  // обновляем "смещения"
        for(h <- 0 until hidden_neurons) who(h)(o) += learn_rate*erro(o)*hidden(h)
      }
      
      // обновляем веса от входного слоя к скрытому
      for(h <- 0 until hidden_neurons) {        
        wih(input_neurons)(h) += learn_rate*errh(h)   // обновляем "смещения"
        for(i <- 0 until input_neurons) wih(i)(h) += learn_rate*errh(h)*inputs(i)
      }

      // вычисляем общую ошибку и если она больше текущей для всей тренировочной сессии - обновляем текущую
      val error = math.sqrt((0 until output_neurons).foldLeft(0.0)((sum, o) => sum + math.pow(expected(o) - actual(o), 2)))
      //reporter.report("["+iteration+"] current erro: "+error)      
      if(error > current_worst_erro) {
        current_worst_erro = error
        //reporter("["+iteration+"] current worst erro: "+current_worst_erro)
      }
      
      next_in_set += 1
      if(next_in_set >= training_session.length) {  // закончилась очередная тренировочная сессия
        if(current_worst_erro < worst_erro) {       // сравниваем наихудшую ошибку за сессию с общей наименьшей ошибкой за все пройденные сессии
          best_who = who                            // если новая ошибка оказалась меньше - сохраняем веса
          best_wih = wih
          worst_erro = current_worst_erro
          //sessions_without_correction = 0
          reporter("["+iteration+"] current worst erro: "+current_worst_erro)
        }/* else sessions_without_correction += 1*/
        
        /*if(sessions_without_correction > 20) {                            // 20 тренировочных сессий подряд ошибка не уменьшалась - сбрасываем веса
          reporter("["+iteration+"] assigning random weights...")
          assignRandomWeights()
          worst_erro = 100.0
          sessions_without_correction = 0
        }*/
        
        current_worst_erro = 0
        next_in_set = 0
        next_session += 1
      }
      iteration += 1
    }

    // восстанавливаем лучшие веса, полученные в ходе обучения
    who = best_who
    wih = best_wih
    /*wch = best_wch
    hidden = best_hidden
    context = best_context*/
    reporter("worst erro: "+worst_erro)
  }

  def save(file:String) {
    val fos = new FileOutputStream(file)
    val sb = new StringBuilder
    sb.append("input_neurons\n")
    sb.append(input_neurons+"\n")
    sb.append("---\n")

    sb.append("hidden_neurons\n")
    sb.append(hidden_neurons+"\n")
    sb.append("---\n")

    sb.append("output_neurons\n")
    sb.append(output_neurons+"\n")
    sb.append("---\n")

    sb.append("wih\n")
    for {
      row <- wih
      elem <- row
    } sb.append(elem+"\n")
    sb.append("---\n")

    sb.append("wch\n")
    for {
      row <- wch
      elem <- row
    } sb.append(elem+"\n")
    sb.append("---\n")

    sb.append("who\n")
    for {
      row <- who
      elem <- row
    } sb.append(elem+"\n")
    sb.append("---\n")

    sb.append("whc\n")
    for {
      elem <- whc
    } sb.append(elem+"\n")
    sb.append("---\n")

    sb.append("context\n")
    for {
      elem <- context
    } sb.append(elem+"\n")
    sb.append("---\n")

    fos.write(sb.toString().getBytes)
    fos.flush()
    fos.close()
  }

  def load(file:String) {
    val reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))
    var i, j = 0
    var current_loading_array = ""
    while(reader.ready) {
      val line = reader.readLine
      line match {
        case "input_neurons" | "hidden_neurons" | "output_neurons" => current_loading_array = ""
        case "wih" => current_loading_array = "wih"
        case "wch" => current_loading_array = "wch"
        case "who" => current_loading_array = "who"
        case "whc" => current_loading_array = "whc"
        case "context" => current_loading_array = "context"
        case "---" =>
          i = 0; j = 0
        case elem =>
          current_loading_array match {
            case "wih" => _insertElemTo2DimArray(wih, elem.toDouble)
            case "wch" => _insertElemTo2DimArray(wch, elem.toDouble)
            case "who" => _insertElemTo2DimArray(who, elem.toDouble)
            case "whc" =>
              whc(i) = elem.toDouble
              i += 1
            case "context" =>
              context(i) = elem.toDouble
              i += 1
            case _ => //do nothing
          }
      }
    }

    def _insertElemTo2DimArray(arr:Array[Array[Double]], elem:Double) {
      arr(i)(j) = elem
      j += 1
      if(j >= arr(0).length) {
        i += 1
        j = 0
      }
    }
  }
}

/**
 * Программа проводит простые синтетические тесты сети Элмана. В качестве входных значений берутся двоичные числа от 1 до 6.
 * В качестве отклика ожидаются десятичные значения.
 */
object TestElman extends App {
  val training_session = Array(
    TrainingSet(Array(0,0,1), Array(0,0,0,0,0,1)),
    TrainingSet(Array(0,1,0), Array(0,0,0,0,1,0)),
    TrainingSet(Array(0,1,1), Array(0,0,0,1,0,0)),
    TrainingSet(Array(1,0,0), Array(0,0,1,0,0,0)),
    TrainingSet(Array(1,0,1), Array(0,1,0,0,0,0)),
    TrainingSet(Array(1,1,0), Array(1,0,0,0,0,0))
  )
  
  val elman = new Elman(3, 3, 6)
  elman.train(training_session, error_threshold = 0.75)

  // test network
  println("---------------------------")
  var error = 0.0
  for(i <- 0 until 100000) {
    val random_set = (math.random*training_session.length).toInt
    val TrainingSet(inputs, expected) = training_session(random_set)
    val actual = elman.outputs(inputs)
    val actual_max = actual.max
    val answer = actual.map(elem => if(elem == actual_max) 1 else 0)
    val answer_diff = answer.zip(expected).map {
      case (answer_elem, expected_elem) => answer_elem - expected_elem
    }
    if(answer_diff.exists(_ != 0)) error += 1
    println(inputs.mkString("Inputs(",", ",")")+": "+actual.mkString("Actual(",", ",")")+" "+answer.mkString("Answer(",", ",")")+" "+expected.mkString("Expected(",", ",")")+" "+answer_diff.mkString("Diff(",", ",")"))
  }
  println(error/100000.0)
}
