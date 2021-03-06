package spittr.web;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import spittr.dao.SpitterRepository;
import spittr.dao.SpittleRepository;
import spittr.dto.Spitter;
import spittr.dto.Spittle;
import spittr.service.UserContext;

@Controller
@RequestMapping("/spittles")
public class SpittleController {

	private static final String MAX_LONG_AS_STRING = "9223372036854775807";

	private SpittleRepository spittleRepository;

	@Autowired
	private SpitterRepository spitterRepository;

	@Autowired
	public SpittleController(SpittleRepository spittleRepository) {
		this.spittleRepository = spittleRepository;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<Spittle> spittles(@RequestParam(value = "max", defaultValue = MAX_LONG_AS_STRING) long max,
			@RequestParam(value = "count", defaultValue = "20") int count) {
		return spittleRepository.findRecentSpittles(count);
	}

	@RequestMapping(value = "/{spittleId}", method = RequestMethod.GET)
	public String spittle(@PathVariable("spittleId") long spittleId, Model model) {
		Spittle spittle = spittleRepository.findOne(spittleId);
		if (spittle == null) {
			throw new SpittleNotFoundException();
		}
		model.addAttribute(spittle);
		return "spittle";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String saveSpittle(SpittleForm form, Model model) {
		try {
			long userId = UserContext.getInstance().getUserId();
			Spitter spitter = spitterRepository.findOne(userId);
			Long spittleId = new Date().getTime();
			spittleRepository.save(new Spittle(spittleId, spitter, form.getMessage(), new Date()));
			return "redirect:/spittles";
		} catch (DuplicateSpittleException e) {
			return "error/duplicate";
		}
	}

	@ExceptionHandler(DuplicateSpittleException.class)
	public String handleNotFound() {
		return "error/duplicate";
	}

}
